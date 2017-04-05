/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchema.ObjectType.*;

/**
 * Oracle implementation of Schema.
 */
public class OracleSchema extends Schema<OracleDbSupport> {
    private static final Log LOG = LogFactory.getLog(OracleSchema.class);

    /**
     * Creates a new Oracle schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public OracleSchema(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    /**
     * Checks whether the schema is system, i.e. Oracle-maintained, or not.
     *
     * @return {@code true} if it is system, {@code false} if not.
     */
    protected boolean isSystem() throws SQLException {
        return dbSupport.getSystemSchemas().contains(name);
    }

    /**
     * Checks whether this schema is default for the current user.
     *
     * @return {@code true} if it is default, {@code false} if not.
     */
    protected boolean isDefaultSchemaForUser() throws SQLException {
        return name.equals(dbSupport.getCurrentUserName());
    }

    @Override
    protected boolean doExists() throws SQLException {
        return dbSupport.queryReturnsRows("SELECT * FROM ALL_USERS WHERE USERNAME = ?", name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return !dbSupport.queryReturnsRows("SELECT * FROM ALL_OBJECTS WHERE OWNER = ?", name);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE USER " + dbSupport.quote(name) + " IDENTIFIED BY flyway");
        jdbcTemplate.execute("GRANT RESOURCE TO " + dbSupport.quote(name));
        jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP USER " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        if (isSystem()) {
            throw new FlywayException("Clean not supported on Oracle for system schema " + dbSupport.quote(name) + "! " +
                    "It must not be changed in any way except by running an Oracle-supplied script!");
        }

        // Disable FBA for schema tables.
        if (dbSupport.isFlashbackDataArchiveAvailable()) {
            disableFlashbackArchiveForFbaTrackedTables();
        }

        // Clean Oracle Locator metadata.
        if (dbSupport.isLocatorAvailable()) {
            cleanLocatorMetadata();
        }

        // Get existing object types in the schema.
        Set<String> existingObjectTypeNames = getExistingObjectTypeNames();

        // Define the list of types to drop.
        List<ObjectType> objectTypesToDrop = Arrays.asList(
                TRIGGER,
                QUEUE_TABLE,
                SCHEDULER_JOB,
                MATERIALIZED_VIEW,
                MATERIALIZED_VIEW_LOG,
                VIEW,
                DOMAIN_INDEX,
                TABLE,
                INDEX,
                CLUSTER,
                SEQUENCE,
                FUNCTION,
                PROCEDURE,
                PACKAGE,
                TYPE,
                SYNONYM,
                JAVA_SOURCE
        );

        for (ObjectType objectType : objectTypesToDrop) {
            if (existingObjectTypeNames.contains(objectType.getName())) {
                objectType.dropObjects(jdbcTemplate, dbSupport, this);
            }
        }

        if (isDefaultSchemaForUser()) {
            jdbcTemplate.execute("PURGE RECYCLEBIN");
        }
    }

    /**
     * Returns the schema's objects grouped in lists by object type (value of ALL_OBJECTS.OBJECT_TYPE column).
     * @return a map of type names to object name lists
     * @throws SQLException if retrieving of objects failed
     */
    private Set<String> getExistingObjectTypeNames() throws SQLException {
        String query = "SELECT DISTINCT OBJECT_TYPE FROM ALL_OBJECTS WHERE OWNER = ? " +
                // Materialized view logs are seen as ordinary tables, look up in another view.
                "UNION SELECT '" + MATERIALIZED_VIEW_LOG.getName() + "' FROM DUAL WHERE EXISTS(" +
                "SELECT * FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?) " +
                // Queue tables are seen as ordinary tables, look up in another view.
                "UNION SELECT '" + QUEUE_TABLE.getName() + "' FROM DUAL WHERE EXISTS(" +
                "SELECT * FROM ALL_QUEUE_TABLES WHERE OWNER = ?)";

        String[] params = new String[3];
        Arrays.fill(params, this.getName());

        return new HashSet<String>(jdbcTemplate.queryForStringList(query, params));
    }

    /**
     * Executes ALTER statements for all tables that have Flashback Archive enabled.
     * Flashback Archive is an asynchronous process so we need to wait until it completes, otherwise cleaning the
     * tables in schema will sometimes fail with ORA-55622 or ORA-55610 depending on the race between
     * Flashback Archive and Java code.
     *
     * @throws SQLException when the statements could not be generated.
     */
    private void disableFlashbackArchiveForFbaTrackedTables() throws SQLException {
        // DBA_FLASHBACK_ARCHIVE_TABLES is granted to PUBLIC
        String queryForFbaTrackedTables = "SELECT TABLE_NAME FROM DBA_FLASHBACK_ARCHIVE_TABLES WHERE OWNER_NAME = ?";
        List<String> tableNames = jdbcTemplate.queryForStringList(queryForFbaTrackedTables, name);
        for (String tableName : tableNames) {
            jdbcTemplate.execute("ALTER TABLE " + dbSupport.quote(name, tableName) + " NO FLASHBACK ARCHIVE");
            //wait until the tables disappear
            while (dbSupport.queryReturnsRows(queryForFbaTrackedTables + " AND TABLE_NAME = ?", name, tableName)) {
                try {
                    LOG.debug("Actively waiting for Flashback cleanup on table: " + dbSupport.quote(name, tableName));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback cleanup interrupted", e);
                }
            }
        }
    }

    /**
     * Checks whether Oracle Locator metadata exists for the schema.
     *
     * @return {@code true} if it exists, {@code false} if not.
     * @throws SQLException when checking checking metadata existence failed.
     */
    private boolean locatorMetadataExists() throws SQLException {
        return dbSupport.queryReturnsRows("SELECT * FROM ALL_SDO_GEOM_METADATA WHERE OWNER = ?", name);
    }

    /**
     * Clean Oracle Locator metadata for the schema. Works only for the user's default schema, prints a warning message
     * to log otherwise.
     *
     * @throws SQLException when performing cleaning failed.
     */
    private void cleanLocatorMetadata() throws SQLException {
        if (!locatorMetadataExists()) {
            return;
        }

        if (!isDefaultSchemaForUser()) {
            LOG.warn("Unable to clean Oracle Locator metadata for schema " + dbSupport.quote(name) +
                    " by user \"" + dbSupport.getCurrentUserName() + "\": unsupported operation");
            return;
        }

        jdbcTemplate.getConnection().commit();
        jdbcTemplate.execute("DELETE FROM USER_SDO_GEOM_METADATA");
        jdbcTemplate.getConnection().commit();
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = TABLE.getObjectNames(jdbcTemplate, dbSupport, this);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new OracleTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new OracleTable(jdbcTemplate, dbSupport, this, tableName);
    }


    /**
     * Oracle object types.
     */
    public enum ObjectType {

        // Tables, including XML tables, except for nested tables, IOT overflow tables and other secondary objects.
        TABLE("TABLE", "CASCADE CONSTRAINTS PURGE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
                boolean referencePartitionedTablesExist = dbSupport.queryReturnsRows(
                        "SELECT * FROM ALL_PART_TABLES WHERE OWNER = ? AND PARTITIONING_TYPE = 'REFERENCE'",
                        schema.getName());
                boolean xmlDbAvailable = dbSupport.isXmlDbAvailable();

                StringBuilder tablesQuery = new StringBuilder();
                tablesQuery.append("WITH TABLES AS (\n" +
                        "  SELECT TABLE_NAME, OWNER\n" +
                        "  FROM ALL_TABLES\n" +
                        "  WHERE OWNER = ?\n" +
                        "    AND (IOT_TYPE IS NULL OR IOT_TYPE NOT LIKE '%OVERFLOW%')\n" +
                        "    AND NESTED != 'YES'\n" +
                        "    AND SECONDARY != 'Y'\n");

                if (xmlDbAvailable) {
                    tablesQuery.append("  UNION ALL\n" +
                            "  SELECT TABLE_NAME, OWNER\n" +
                            "  FROM ALL_XML_TABLES\n" +
                            "  WHERE OWNER = ?\n" +
                            // ALL_XML_TABLES shows objects in RECYCLEBIN, ignore them
                            "    AND TABLE_NAME NOT LIKE 'BIN$________________________$_'\n");
                }

                tablesQuery.append(")\n" +
                        "SELECT t.TABLE_NAME\n" +
                        "FROM TABLES t\n");

                if (referencePartitionedTablesExist) {
                    tablesQuery.append("  LEFT JOIN ALL_PART_TABLES pt\n" +
                            "    ON t.OWNER = pt.OWNER\n" +
                            "   AND t.TABLE_NAME = pt.TABLE_NAME\n" +
                            "   AND pt.PARTITIONING_TYPE = 'REFERENCE'\n" +
                            "  LEFT JOIN ALL_CONSTRAINTS fk\n" +
                            "    ON pt.OWNER = fk.OWNER\n" +
                            "   AND pt.TABLE_NAME = fk.TABLE_NAME\n" +
                            "   AND pt.REF_PTN_CONSTRAINT_NAME = fk.CONSTRAINT_NAME\n" +
                            "   AND fk.CONSTRAINT_TYPE = 'R'\n" +
                            "  LEFT JOIN ALL_CONSTRAINTS puk\n" +
                            "    ON fk.R_OWNER = puk.OWNER\n" +
                            "   AND fk.R_CONSTRAINT_NAME = puk.CONSTRAINT_NAME\n" +
                            "   AND puk.CONSTRAINT_TYPE IN ('P', 'U')\n" +
                            "  LEFT JOIN TABLES p\n" +
                            "    ON puk.OWNER = p.OWNER\n" +
                            "   AND puk.TABLE_NAME = p.TABLE_NAME\n" +
                            "START WITH p.TABLE_NAME IS NULL\n" +
                            "CONNECT BY PRIOR t.TABLE_NAME = p.TABLE_NAME\n" +
                            "ORDER BY LEVEL DESC");
                }

                // Count params
                int n = 1; // ALL_TABLES
                if (xmlDbAvailable) n += 1; // ALL_XML_TABLES
                String[] params = new String[n];
                Arrays.fill(params, schema.getName());

                return jdbcTemplate.queryForStringList(tablesQuery.toString(), params);
            }
        },

        // Queue tables, have related objects and should be dropped separately prior to other types.
        QUEUE_TABLE("QUEUE TABLE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT QUEUE_TABLE FROM ALL_QUEUE_TABLES WHERE OWNER = ?",
                        schema.getName()
                );
            }
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_AQADM.DROP_QUEUE_TABLE('" + dbSupport.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        // Materialized view logs, seen as ordinary tables but should be dropped in a special way.
        MATERIALIZED_VIEW_LOG("MATERIALIZED VIEW LOG") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT MASTER FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?",
                        schema.getName()
                );
            }
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " ON " + dbSupport.quote(schema.getName(), objectName);
            }
        },

        // All indexes, except for domain indexes, should be dropped after tables (if any left).
        INDEX("INDEX") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE NOT LIKE '%DOMAIN%'",
                        schema.getName()
                );
            }
        },

        // Domain indexes, have related objects and should be dropped separately prior to tables.
        DOMAIN_INDEX("INDEX", "FORCE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE LIKE '%DOMAIN%'",
                        schema.getName()
                );
            }
        },

        // Clusters.
        CLUSTER("CLUSTER", "INCLUDING TABLES CASCADE CONSTRAINTS"),

        // Views, including XML views.
        VIEW("VIEW", "CASCADE CONSTRAINTS"),

        // Materialized views, keep tables as they may be referenced.
        MATERIALIZED_VIEW("MATERIALIZED VIEW", "PRESERVE TABLE"),

        // Local synonyms.
        SYNONYM("SYNONYM", "FORCE"),

        // Sequences, no filtering for identity sequences, since they get dropped along with master tables..
        SEQUENCE("SEQUENCE"),

        // Procedures, functions, packages.
        PROCEDURE("PROCEDURE"),
        FUNCTION("FUNCTION"),
        PACKAGE("PACKAGE"),

        // Triggers of all types, should be dropped at first, because invalid DDL triggers may break the whole clean.
        TRIGGER("TRIGGER"),

        // Types, should be dropped at the end.
        TYPE("TYPE", "FORCE"),

        // Java sources, cause cascade drop of object classes (only those compiled from sources).
        JAVA_SOURCE("JAVA SOURCE"),

        // Scheduler jobs.
        SCHEDULER_JOB("JOB") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_JOB('" + dbSupport.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        }
        // TODO: to be extended
        ;


        private final String name;
        private final String dropOptions;

        ObjectType(String name, String dropOptions) {
            this.name = name;
            this.dropOptions = dropOptions;
        }

        ObjectType(String name) {
            this(name, "");
        }

        public String getName() {
            return name;
        }

        public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList(
                    "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = ?",
                    schema.getName(), this.getName()
            );
        }

        public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema, String objectName) {
            return "DROP " + this.getName() + " " + dbSupport.quote(schema.getName(), objectName) +
                    (StringUtils.hasText(dropOptions) ? " " + dropOptions : "");
        }

        public void dropObjects(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, OracleSchema schema) throws SQLException {
            for (String objectName : getObjectNames(jdbcTemplate, dbSupport, schema)) {
                jdbcTemplate.execute(generateDropStatement(jdbcTemplate, dbSupport, schema, objectName));
            }
        }
    }
}
