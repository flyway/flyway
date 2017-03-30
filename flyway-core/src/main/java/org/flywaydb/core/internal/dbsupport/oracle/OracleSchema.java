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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Disable FBA for schema tables
        if (dbSupport.isFlashbackDataArchiveAvailable()) {
            disableFlashbackArchiveForFbaTrackedTables();
        }

        // Cleanup Oracle Locator metadata
        if (dbSupport.isLocatorAvailable()) {
            cleanLocatorMetadata();
        }

        // Take a snapshot of existing objects grouped by type.
        Map<String, List<String>> objectsByType = getObjectsGroupedByType();


        if (objectsByType.containsKey("TRIGGER"))
            for (String statement : generateDropStatementsForObjectType("TRIGGER", "", objectsByType.get("TRIGGER"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("TABLE"))
            for (String statement : generateDropStatementsForQueueTables()) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("JOB"))
            for (String statement : generateDropStatementsForSchedulerJobs(objectsByType.get("JOB"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("MATERIALIZED VIEW"))
            for (String statement : generateDropStatementsForObjectType("MATERIALIZED VIEW", "PRESERVE TABLE", objectsByType.get("MATERIALIZED VIEW"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("TABLE"))
            for (String statement : generateDropStatementsForMaterializedViewLogs()) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("VIEW"))
            for (String statement : generateDropStatementsForObjectType("VIEW", "CASCADE CONSTRAINTS")) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("INDEX"))
            for (String statement : generateDropStatementsForDomainIndexes()) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("TABLE"))
            for (String statement : generateDropStatementsForXmlTables()) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("TABLE"))
            for (Table table : allTables()) {
                table.drop();
            }

        if (objectsByType.containsKey("INDEX"))
            for (String statement : generateDropStatementsForNonDomainIndexes()) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("CLUSTER"))
            for (String statement : generateDropStatementsForObjectType("CLUSTER", "INCLUDING TABLES CASCADE CONSTRAINTS", objectsByType.get("CLUSTER"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("SEQUENCE"))
            for (String statement : generateDropStatementsForObjectType("SEQUENCE", "")) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("FUNCTION"))
            for (String statement : generateDropStatementsForObjectType("FUNCTION", "", objectsByType.get("FUNCTION"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("PROCEDURE"))
            for (String statement : generateDropStatementsForObjectType("PROCEDURE", "", objectsByType.get("PROCEDURE"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("PACKAGE"))
            for (String statement : generateDropStatementsForObjectType("PACKAGE", "", objectsByType.get("PACKAGE"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("TYPE"))
            for (String statement : generateDropStatementsForObjectType("TYPE", "FORCE")) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("SYNONYM"))
            for (String statement : generateDropStatementsForObjectType("SYNONYM",  "FORCE", objectsByType.get("SYNONYM"))) {
                jdbcTemplate.execute(statement);
            }

        if (objectsByType.containsKey("JAVA SOURCE"))
            for (String statement : generateDropStatementsForObjectType("JAVA SOURCE", "", objectsByType.get("JAVA SOURCE"))) {
                jdbcTemplate.execute(statement);
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
    private Map<String, List<String>> getObjectsGroupedByType() throws SQLException {
        boolean xmlDbAvailable = dbSupport.isXmlDbAvailable();
        String query =
                // Most of objects are seen in ALL_OBJECTS
                "SELECT OBJECT_TYPE, OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? " +
                        (xmlDbAvailable
                                // XML tables are seen in a separate dictionary table
                                ? "UNION ALL SELECT 'TABLE', TABLE_NAME FROM ALL_XML_TABLES WHERE OWNER = ? " +
                                "AND TABLE_NAME NOT LIKE 'BIN$________________________$_'" //ignore recycle bin objects
                                : "");

        // Count params
        int n = 1;
        if (xmlDbAvailable) n += 1;
        String[] params = new String[n];
        Arrays.fill(params, name);

        List<Map<String, String>> rows = jdbcTemplate.queryForList(query, params);
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (Map<String, String> row : rows) {
            String objectType = row.get("OBJECT_TYPE");
            String objectName = row.get("OBJECT_NAME");
            if (result.containsKey(objectType)) {
                result.get(objectType).add(objectName);
            } else {
                List<String> newList = new ArrayList<String>();
                newList.add(objectName);
                result.put(objectType, newList);
            }
        }
        return result;
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

    /**
     * Generates the drop statement for the database object.
     *
     * @param typeName    The type of database object to drop.
     * @param objectName  The name of database object to drop.
     * @param dropOptions The extra arguments to add to the drop statement.
     * @return The complete drop statement, ready to execute.
     * @throws SQLException when the drop statement could not be generated.
     */
    private String generateDefaultDropStatement(String typeName, String objectName, String dropOptions) {
        return "DROP " + typeName + " " + dbSupport.quote(name, objectName) + " " +
                (StringUtils.hasText(dropOptions) ? dropOptions : "");
    }

    /**
     * Get the list of database objects of this type.
     *
     * @param typeName The type of database objects.
     * @return The list of such objects in the schema.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> getObjectsByType(String typeName) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = ?",
                name, typeName);
    }

    /**
     * Generates the drop statements for database objects of this type.
     *
     * @param typeName    The type of database object to drop.
     * @param dropOptions The extra arguments to add to the drop statement.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForObjectType(String typeName, String dropOptions) throws SQLException {
        return generateDropStatementsForObjectType(typeName, dropOptions, getObjectsByType(typeName));
    }

    /**
     * Generates the drop statements for database objects of this type in a prefetched list of objects.
     *
     * @param typeName    The type of database object to drop.
     * @param dropOptions The extra arguments to add to the drop statement.
     * @param prefetchedObjects The list of object names.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForObjectType(String typeName, String dropOptions, List<String> prefetchedObjects) throws SQLException {
        List<String> dropStatements = new ArrayList<String>();
        for (String objectName : prefetchedObjects) {
            dropStatements.add(generateDefaultDropStatement(typeName, objectName, dropOptions));
        }
        return dropStatements;
    }

    /**
     * Generates the drop statements for queue tables.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForQueueTables() throws SQLException {
        List<String> statements = new ArrayList<String>();

        List<String> objectNames = jdbcTemplate.queryForStringList("SELECT QUEUE_TABLE FROM ALL_QUEUE_TABLES WHERE OWNER = ?", name);
        for (String objectName : objectNames) {
            statements.add("BEGIN DBMS_AQADM.DROP_QUEUE_TABLE('" + dbSupport.quote(name, objectName) + "', FORCE => TRUE); END;");
        }

        return statements;
    }

    /**
     * Generates the drop statements for scheduler jobs.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForSchedulerJobs() throws SQLException {
        return generateDropStatementsForSchedulerJobs(getObjectsByType("JOB"));
    }

    /**
     * Generates the drop statements for scheduler jobs in a prefetched list of objects.
     *
     * @param prefetchedObjects The list of object names.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForSchedulerJobs(List<String> prefetchedObjects) throws SQLException {
        List<String> statements = new ArrayList<String>();

        for (String objectName : prefetchedObjects) {
            statements.add("BEGIN DBMS_SCHEDULER.DROP_JOB('" + dbSupport.quote(name, objectName) + "', FORCE => TRUE); END;");
        }

        return statements;
    }

    /**
     * Generates the drop statements for materialized view logs.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForMaterializedViewLogs() throws SQLException {
        List<String> dropStatements = new ArrayList<String>();

        List<String> objectNames = jdbcTemplate.queryForStringList(
                "SELECT MASTER FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?", name);

        for (String objectName : objectNames) {
            dropStatements.add(generateDefaultDropStatement("MATERIALIZED VIEW LOG ON", objectName, ""));
        }
        return dropStatements;
    }

    /**
     * Generates the drop statements for domain indexes.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForDomainIndexes() throws SQLException {
        List<String> dropStatements = new ArrayList<String>();

        List<String> objectNames = jdbcTemplate.queryForStringList(
                "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE LIKE '%DOMAIN%'", name);

        for (String objectName : objectNames) {
            dropStatements.add(generateDefaultDropStatement("INDEX", objectName, "FORCE"));
        }
        return dropStatements;
    }

    /**
     * Generates the drop statements for non-domain indexes.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForNonDomainIndexes() throws SQLException {
        List<String> dropStatements = new ArrayList<String>();

        List<String> objectNames = jdbcTemplate.queryForStringList(
                "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE NOT LIKE '%DOMAIN%'", name);

        for (String objectName : objectNames) {
            dropStatements.add(generateDefaultDropStatement("INDEX", objectName, ""));
        }
        return dropStatements;
    }

    /**
     * Generates the drop statements for xml tables.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForXmlTables() throws SQLException {
        List<String> dropStatements = new ArrayList<String>();

        List<String> objectNames = dbSupport.isXmlDbAvailable()
                ? jdbcTemplate.queryForStringList(
                "SELECT TABLE_NAME FROM ALL_XML_TABLES WHERE OWNER = ? " +
                        // ALL_XML_TABLES shows objects in RECYCLEBIN, ignore them
                        "AND TABLE_NAME NOT LIKE 'BIN$________________________$_'",
                name)
                : Collections.<String>emptyList();

        for (String objectName : objectNames) {
            dropStatements.add(generateDefaultDropStatement("TABLE", objectName, "CASCADE CONSTRAINTS PURGE"));
        }
        return dropStatements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        String tablesQuery =
                "SELECT TABLE_NAME, OWNER\n" +
                        "FROM ALL_TABLES\n" +
                        "WHERE OWNER = ?\n" +
                        "  AND (IOT_TYPE IS NULL OR IOT_TYPE NOT LIKE '%OVERFLOW%')\n" +
                        "  AND NESTED != 'YES'\n" +
                        "  AND SECONDARY != 'Y'\n";

        boolean referencePartitionedTablesExist = dbSupport.queryReturnsRows(
                "SELECT * FROM ALL_PART_TABLES WHERE OWNER = ? AND PARTITIONING_TYPE = 'REFERENCE'",
                name);

        if (referencePartitionedTablesExist) {
            tablesQuery =
                    "WITH TABLES AS (\n" +
                    tablesQuery +
                    ")\n" +
                    "SELECT t.TABLE_NAME\n" +
                    "FROM TABLES t\n" +
                    "  LEFT JOIN ALL_PART_TABLES pt\n" +
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
                    "ORDER BY LEVEL DESC";
        }

        List<String> tableNames = jdbcTemplate.queryForStringList(tablesQuery, name);

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
}
