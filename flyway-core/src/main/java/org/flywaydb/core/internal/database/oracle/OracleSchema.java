/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.flywaydb.core.internal.database.oracle.OracleSchema.ObjectType.*;

/**
 * Oracle implementation of Schema.
 */
public class OracleSchema extends Schema<OracleDatabase> {
    private static final Log LOG = LogFactory.getLog(OracleSchema.class);

    /**
     * Creates a new Oracle schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    OracleSchema(JdbcTemplate jdbcTemplate, OracleDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    /**
     * Checks whether the schema is system, i.e. Oracle-maintained, or not.
     *
     * @return {@code true} if it is system, {@code false} if not.
     */
    public boolean isSystem() throws SQLException {
        return database.getSystemSchemas().contains(name);
    }

    /**
     * Checks whether this schema is default for the current user.
     *
     * @return {@code true} if it is default, {@code false} if not.
     */
    boolean isDefaultSchemaForUser() throws SQLException {
        return name.equals(database.doGetCurrentUser());
    }

    @Override
    protected boolean doExists() throws SQLException {
        return database.queryReturnsRows("SELECT * FROM ALL_USERS WHERE USERNAME = ?", name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return !supportedTypesExist(jdbcTemplate, database, this);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE USER " + database.quote(name) + " IDENTIFIED BY "
                + database.quote("FFllyywwaayy00!!"));
        jdbcTemplate.execute("GRANT RESOURCE TO " + database.quote(name));
        jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP USER " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        if (isSystem()) {
            throw new FlywayException("Clean not supported on Oracle for system schema " + database.quote(name) + "! " +
                    "It must not be changed in any way except by running an Oracle-supplied script!");
        }

        // Disable FBA for schema tables.
        if (database.isFlashbackDataArchiveAvailable()) {
            disableFlashbackArchiveForFbaTrackedTables();
        }

        // Clean Oracle Locator metadata.
        if (database.isLocatorAvailable()) {
            cleanLocatorMetadata();
        }

        // Get existing object types in the schema.
        Set<String> objectTypeNames = getObjectTypeNames(jdbcTemplate, database, this);

        // Define the list of types to process, order is important.
        List<ObjectType> objectTypesToClean = Arrays.asList(
                // Types to drop.
                TRIGGER,
                QUEUE_TABLE,
                FILE_WATCHER,
                SCHEDULER_CHAIN,
                SCHEDULER_JOB,
                SCHEDULER_PROGRAM,
                SCHEDULE,
                RULE_SET,
                RULE,
                EVALUATION_CONTEXT,
                FILE_GROUP,
                XML_SCHEMA,
                MINING_MODEL,
                REWRITE_EQUIVALENCE,
                SQL_TRANSLATION_PROFILE,
                MATERIALIZED_VIEW,
                MATERIALIZED_VIEW_LOG,
                DIMENSION,
                VIEW,
                DOMAIN_INDEX,
                DOMAIN_INDEX_TYPE,
                TABLE,
                INDEX,
                CLUSTER,
                SEQUENCE,
                OPERATOR,
                FUNCTION,
                PROCEDURE,
                PACKAGE,
                CONTEXT,
                LIBRARY,
                TYPE,
                SYNONYM,
                JAVA_SOURCE,
                JAVA_CLASS,
                JAVA_RESOURCE,

                // Object types with sensitive information (passwords), skip intentionally, print warning if found.
                DATABASE_LINK,
                CREDENTIAL,

                // Unsupported types, print warning if found
                DATABASE_DESTINATION,
                SCHEDULER_GROUP,
                CUBE,
                CUBE_DIMENSION,
                CUBE_BUILD_PROCESS,
                MEASURE_FOLDER,

                // Undocumented types, print warning if found
                ASSEMBLY,
                JAVA_DATA
        );

        for (ObjectType objectType : objectTypesToClean) {
            if (objectTypeNames.contains(objectType.getName())) {
                LOG.debug("Cleaning objects of type " + objectType + " ...");
                objectType.dropObjects(jdbcTemplate, database, this);
            }
        }

        if (isDefaultSchemaForUser()) {
            jdbcTemplate.execute("PURGE RECYCLEBIN");
        }
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
        boolean dbaViewAccessible = database.isPrivOrRoleGranted("SELECT ANY DICTIONARY")
                || database.isDataDictViewAccessible("DBA_FLASHBACK_ARCHIVE_TABLES");

        if (!dbaViewAccessible && !isDefaultSchemaForUser()) {
            LOG.warn("Unable to check and disable Flashback Archive for tables in schema " + database.quote(name) +
                    " by user \"" + database.doGetCurrentUser() + "\": DBA_FLASHBACK_ARCHIVE_TABLES is not accessible");
            return;
        }

        boolean oracle18orNewer = database.getVersion().isAtLeast("18");

        String queryForFbaTrackedTables = "SELECT TABLE_NAME FROM " + (dbaViewAccessible ? "DBA_" : "USER_")
                + "FLASHBACK_ARCHIVE_TABLES WHERE OWNER_NAME = ?"
                + (oracle18orNewer ? " AND STATUS='ENABLED'" : "");
        List<String> tableNames = jdbcTemplate.queryForStringList(queryForFbaTrackedTables, name);
        for (String tableName : tableNames) {
            jdbcTemplate.execute("ALTER TABLE " + database.quote(name, tableName) + " NO FLASHBACK ARCHIVE");
            //wait until the tables disappear
            while (database.queryReturnsRows(queryForFbaTrackedTables + " AND TABLE_NAME = ?", name, tableName)) {
                try {
                    LOG.debug("Actively waiting for Flashback cleanup on table: " + database.quote(name, tableName));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback cleanup interrupted", e);
                }
            }
        }

        if (oracle18orNewer) {
            while (database.queryReturnsRows("SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ?\n"
                    + " AND TABLE_NAME LIKE 'SYS_FBA_DDL_COLMAP_%'", name)) {
                try {
                    LOG.debug("Actively waiting for Flashback colmap cleanup");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback colmap cleanup interrupted", e);
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
        return database.queryReturnsRows("SELECT * FROM ALL_SDO_GEOM_METADATA WHERE OWNER = ?", name);
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
            LOG.warn("Unable to clean Oracle Locator metadata for schema " + database.quote(name) +
                    " by user \"" + database.doGetCurrentUser() + "\": unsupported operation");
            return;
        }

        jdbcTemplate.getConnection().commit();
        jdbcTemplate.execute("DELETE FROM USER_SDO_GEOM_METADATA");
        jdbcTemplate.getConnection().commit();
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = TABLE.getObjectNames(jdbcTemplate, database, this);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new OracleTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new OracleTable(jdbcTemplate, database, this, tableName);
    }


    /**
     * Oracle object types.
     */
    public enum ObjectType {
        // Tables, including XML tables, except for nested tables, IOT overflow tables and other secondary objects.
        TABLE("TABLE", "CASCADE CONSTRAINTS PURGE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                boolean referencePartitionedTablesExist = database.queryReturnsRows(
                        "SELECT * FROM ALL_PART_TABLES WHERE OWNER = ? AND PARTITIONING_TYPE = 'REFERENCE'",
                        schema.getName());
                boolean xmlDbAvailable = database.isXmlDbAvailable();

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

                // Reference partitioned tables should be dropped in child-to-parent order.
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

                int n = 1 + (xmlDbAvailable ? 1 : 0);
                String[] params = new String[n];
                Arrays.fill(params, schema.getName());

                return jdbcTemplate.queryForStringList(tablesQuery.toString(), params);
            }
        },

        // Queue tables, have related objects and should be dropped separately prior to other types.
        QUEUE_TABLE("QUEUE TABLE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT QUEUE_TABLE FROM ALL_QUEUE_TABLES WHERE OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_AQADM.DROP_QUEUE_TABLE('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        // Materialized view logs.
        MATERIALIZED_VIEW_LOG("MATERIALIZED VIEW LOG") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT MASTER FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " ON " + database.quote(schema.getName(), objectName);
            }
        },

        // All indexes, except for domain indexes, should be dropped after tables (if any left).
        INDEX("INDEX") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ?" +
                                //" AND INDEX_NAME NOT LIKE 'SYS_C%'"+
                                " AND INDEX_TYPE NOT LIKE '%DOMAIN%'",
                        schema.getName()
                );
            }
        },

        // Domain indexes, have related objects and should be dropped separately prior to tables.
        DOMAIN_INDEX("INDEX", "FORCE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE LIKE '%DOMAIN%'",
                        schema.getName()
                );
            }
        },

        // Domain index types.
        DOMAIN_INDEX_TYPE("INDEXTYPE", "FORCE"),

        // Operators.
        OPERATOR("OPERATOR", "FORCE"),

        // Clusters.
        CLUSTER("CLUSTER", "INCLUDING TABLES CASCADE CONSTRAINTS"),

        // Views, including XML views.
        VIEW("VIEW", "CASCADE CONSTRAINTS"),

        // Materialized views, keep tables as they may be referenced.
        MATERIALIZED_VIEW("MATERIALIZED VIEW", "PRESERVE TABLE"),

        // Dimensions.
        DIMENSION("DIMENSION") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT DIMENSION_NAME FROM ALL_DIMENSIONS WHERE OWNER = ?",
                        schema.getName()
                );
            }
        },

        // Local synonyms.
        SYNONYM("SYNONYM", "FORCE"),

        // Sequences, no filtering for identity sequences, since they get dropped along with master tables.
        SEQUENCE("SEQUENCE"),

        // Procedures, functions, packages.
        PROCEDURE("PROCEDURE"),
        FUNCTION("FUNCTION"),
        PACKAGE("PACKAGE"),

        // Contexts, seen in DBA_CONTEXT view, may remain if DBA_CONTEXT is not accessible.
        CONTEXT("CONTEXT") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT NAMESPACE FROM " + database.dbaOrAll("CONTEXT") + " WHERE SCHEMA = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " " + database.quote(objectName); // no owner
            }
        },

        // Triggers of all types, should be dropped at first, because invalid DDL triggers may break the whole clean.
        TRIGGER("TRIGGER"),

        // Types.
        TYPE("TYPE", "FORCE"),

        // Java sources, classes, resources.
        JAVA_SOURCE("JAVA SOURCE"),
        JAVA_CLASS("JAVA CLASS"),
        JAVA_RESOURCE("JAVA RESOURCE"),

        // Libraries.
        LIBRARY("LIBRARY"),

        // XML schemas.
        XML_SCHEMA("XML SCHEMA") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                if (!database.isXmlDbAvailable()) {
                    return Collections.emptyList();
                }
                return jdbcTemplate.queryForStringList(
                        "SELECT QUAL_SCHEMA_URL FROM " + database.dbaOrAll("XML_SCHEMAS") + " WHERE OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_XMLSCHEMA.DELETESCHEMA('" + objectName + "', DELETE_OPTION => DBMS_XMLSCHEMA.DELETE_CASCADE_FORCE); END;";
            }
        },

        // Rewrite equivalences.
        REWRITE_EQUIVALENCE("REWRITE EQUIVALENCE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN SYS.DBMS_ADVANCED_REWRITE.DROP_REWRITE_EQUIVALENCE('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },

        // SQL translation profiles.
        SQL_TRANSLATION_PROFILE("SQL TRANSLATION PROFILE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SQL_TRANSLATOR.DROP_PROFILE('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },

        // Data mining models, have related objects, should be dropped prior to tables.



        MINING_MODEL("MINING MODEL") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {



                    return super.getObjectNames(jdbcTemplate, database, schema);







            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_DATA_MINING.DROP_MODEL('" +



                                database.quote(schema.getName(), objectName)



                        + "'); END;";
            }
        },

        // Scheduler objects.
        SCHEDULER_JOB("JOB") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_JOB('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        SCHEDULER_PROGRAM("PROGRAM") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_PROGRAM('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        SCHEDULE("SCHEDULE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_SCHEDULE('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        SCHEDULER_CHAIN("CHAIN") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_CHAIN('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        FILE_WATCHER("FILE WATCHER") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_FILE_WATCHER('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        // Streams/rule objects.
        RULE_SET("RULE SET") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_RULE_ADM.DROP_RULE_SET('" + database.quote(schema.getName(), objectName) + "', DELETE_RULES => FALSE); END;";
            }
        },
        RULE("RULE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_RULE_ADM.DROP_RULE('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        EVALUATION_CONTEXT("EVALUATION CONTEXT") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_RULE_ADM.DROP_EVALUATION_CONTEXT('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        FILE_GROUP("FILE GROUP") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_FILE_GROUP.DROP_FILE_GROUP('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },


        /*** Below are unsupported object types. They should be dropped explicitly in callbacks if used. ***/

        // Database links and credentials, contain sensitive information (password) and hence not always can be re-created.
        // Intentionally skip them and let the clean callbacks handle them if needed.
        DATABASE_LINK("DATABASE LINK") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT DB_LINK FROM " + database.dbaOrAll("DB_LINKS") + " WHERE OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " " + objectName; // db link name is case-insensitive and needs no owner
            }
        },
        CREDENTIAL("CREDENTIAL") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_CREDENTIAL('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        // Some scheduler types, not supported yet.
        DATABASE_DESTINATION("DESTINATION") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_DATABASE_DESTINATION('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },
        SCHEDULER_GROUP("SCHEDULER GROUP") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_GROUP('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        // OLAP objects, not supported yet.
        CUBE("CUBE") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },
        CUBE_DIMENSION("CUBE DIMENSION") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },
        CUBE_BUILD_PROCESS("CUBE BUILD PROCESS") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()), "cube build processes");
            }
        },
        MEASURE_FOLDER("MEASURE FOLDER") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },

        // Undocumented objects.
        ASSEMBLY("ASSEMBLY") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()), "assemblies");
            }
        },
        JAVA_DATA("JAVA DATA") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },

        // SYS-owned objects, cannot be dropped when a schema gets cleaned, simply ignore them.
        CAPTURE("CAPTURE"),
        APPLY("APPLY"),
        DIRECTORY("DIRECTORY"),
        RESOURCE_PLAN("RESOURCE PLAN"),
        CONSUMER_GROUP("CONSUMER GROUP"),
        JOB_CLASS("JOB CLASS"),
        WINDOWS("WINDOW"),
        EDITION("EDITION"),
        AGENT_DESTINATION("DESTINATION"),
        UNIFIED_AUDIT_POLICY("UNIFIED AUDIT POLICY");

        /**
         * The name of the type as it mentioned in the Data Dictionary and the DROP statement.
         */
        private final String name;

        /**
         * The extra options used in the DROP statement to enforce the operation.
         */
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

        @Override
        public String toString() {
            return super.toString().replace('_', ' ');
        }

        /**
         * Returns the list of object names of this type.
         *
         * @throws SQLException if retrieving of objects failed.
         */
        public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList(
                    "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = ?",
                    schema.getName(), this.getName()
            );
        }

        /**
         * Generates the drop statement for the specified object.
         *
         */
        public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "DROP " + this.getName() + " " + database.quote(schema.getName(), objectName) +
                    (StringUtils.hasText(dropOptions) ? " " + dropOptions : "");
        }

        /**
         * Drops all objects of this type in the specified schema.
         *
         * @throws SQLException if cleaning failed.
         */
        public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            for (String objectName : getObjectNames(jdbcTemplate, database, schema)) {
                jdbcTemplate.execute(generateDropStatement(jdbcTemplate, database, schema, objectName));
            }
        }

        private void warnUnsupported(String schemaName, String typeDesc) {
            LOG.warn("Unable to clean " + typeDesc + " for schema " + schemaName + ": unsupported operation");
        }

        private void warnUnsupported(String schemaName) {
            warnUnsupported(schemaName, this.toString().toLowerCase() + "s");
        }

        /**
         * Returns the schema's existing object types.
         *
         * @return a set of object type names.
         * @throws SQLException if retrieving of object types failed.
         */
        public static Set<String> getObjectTypeNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            boolean xmlDbAvailable = database.isXmlDbAvailable();








            String query =
                    // Most object types can be correctly selected from DBA_/ALL_OBJECTS.
                    "SELECT DISTINCT OBJECT_TYPE FROM " + database.dbaOrAll("OBJECTS") + " WHERE OWNER = ? " +
                            // Materialized view logs.
                            "UNION SELECT '" + MATERIALIZED_VIEW_LOG.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?) " +
                            // Dimensions.
                            "UNION SELECT '" + DIMENSION.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM ALL_DIMENSIONS WHERE OWNER = ?) " +
                            // Queue tables.
                            "UNION SELECT '" + QUEUE_TABLE.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM ALL_QUEUE_TABLES WHERE OWNER = ?) " +
                            // Database links.
                            "UNION SELECT '" + DATABASE_LINK.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM " + database.dbaOrAll("DB_LINKS") + " WHERE OWNER = ?) " +
                            // Contexts.
                            "UNION SELECT '" + CONTEXT.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM " + database.dbaOrAll("CONTEXT") + " WHERE SCHEMA = ?) " +
                            // XML schemas.
                            (xmlDbAvailable
                                    ? "UNION SELECT '" + XML_SCHEMA.getName() + "' FROM DUAL WHERE EXISTS(" +
                                    "SELECT * FROM " + database.dbaOrAll("XML_SCHEMAS") + " WHERE OWNER = ?) "
                                    : "") +
                            // Credentials.



                                    "UNION SELECT '" + CREDENTIAL.getName() + "' FROM DUAL WHERE EXISTS(" +
                                            "SELECT * FROM ALL_SCHEDULER_CREDENTIALS WHERE OWNER = ?) "








                    ;

            int n = 6 + (xmlDbAvailable ? 1 : 0) +



                            1



                    ;
            String[] params = new String[n];
            Arrays.fill(params, schema.getName());

            return new HashSet<>(jdbcTemplate.queryForStringList(query, params));
        }

        /**
         * Checks whether the specified schema contains object types that can be cleaned.
         *
         * @return {@code true} if it contains, {@code false} if not.
         * @throws SQLException if retrieving of object types failed.
         */
        public static boolean supportedTypesExist(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            Set<String> existingTypeNames = new HashSet<>(getObjectTypeNames(jdbcTemplate, database, schema));

            // Remove unsupported types.
            existingTypeNames.removeAll(Arrays.asList(
                    DATABASE_LINK.getName(),
                    CREDENTIAL.getName(),
                    DATABASE_DESTINATION.getName(),
                    SCHEDULER_GROUP.getName(),
                    CUBE.getName(),
                    CUBE_DIMENSION.getName(),
                    CUBE_BUILD_PROCESS.getName(),
                    MEASURE_FOLDER.getName(),
                    ASSEMBLY.getName(),
                    JAVA_DATA.getName()
            ));

            return !existingTypeNames.isEmpty();
        }
    }
}