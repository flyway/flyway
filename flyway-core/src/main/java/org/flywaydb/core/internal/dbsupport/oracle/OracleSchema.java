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
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

        if (isDefaultSchemaForUser()) {
            for (String statement : generateDropStatementsForQueueTables()) {
                try {
                    jdbcTemplate.execute(statement);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 65040) {
                        //for dropping queue tables, a special grant is required:
                        //GRANT EXECUTE ON DBMS_AQADM TO flyway;
                        LOG.error("Missing required grant to clean queue tables: GRANT EXECUTE ON DBMS_AQADM");
                    }
                    throw e;
                }
            }
        }

        for (String statement : generateDropStatementsForScheduledJobs()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("TRIGGER", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("SEQUENCE", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("FUNCTION", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("MATERIALIZED VIEW", "PRESERVE TABLE")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("PACKAGE", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("PROCEDURE", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("SYNONYM", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("VIEW", "CASCADE CONSTRAINTS")) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForXmlTables()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("CLUSTER", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("TYPE", "FORCE")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("JAVA SOURCE", "")) {
            jdbcTemplate.execute(statement);
        }

        jdbcTemplate.execute("PURGE RECYCLEBIN");
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
     * Generates the drop statements for all xml tables.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForXmlTables() throws SQLException {
        List<String> dropStatements = new ArrayList<String>();

        if (!xmlDBExtensionsAvailable()) {
            LOG.debug("Oracle XML DB Extensions are not available. No cleaning of XML tables.");
            return dropStatements;
        }

        List<String> objectNames =
                jdbcTemplate.queryForStringList("SELECT table_name FROM all_xml_tables WHERE owner = ?", name);
        for (String objectName : objectNames) {
            dropStatements.add("DROP TABLE " + dbSupport.quote(name, objectName) + " PURGE");
        }
        return dropStatements;
    }

    /**
     * Checks whether Oracle XML DB extensions are available or not.
     *
     * @return {@code true} if they are available, {@code false} if not.
     * @throws SQLException when checking availability of the extensions failed.
     */
    private boolean xmlDBExtensionsAvailable() throws SQLException {
        return dbSupport.queryReturnsRows("SELECT * FROM ALL_USERS WHERE USERNAME = 'XDB'")
                && dbSupport.queryReturnsRows("SELECT * FROM ALL_VIEWS WHERE VIEW_NAME = 'RESOURCE_VIEW'");
    }

    /**
     * Generates the drop statements for all database objects of this type.
     *
     * @param objectType     The type of database object to drop.
     * @param extraArguments The extra arguments to add to the drop statement.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForObjectType(String objectType, String extraArguments) throws SQLException {
        String query = "SELECT object_name FROM all_objects WHERE object_type = ? AND owner = ?"
                // Ignore Spatial Index Sequences as they get dropped automatically when the index gets dropped.
                + " AND object_name NOT LIKE 'MDRS_%$'"
                // Ignore Oracle 12 Identity Sequences as they get dropped automatically when the recycle bin gets purged.
                + " AND object_name NOT LIKE 'ISEQ$$_%'";

        List<String> objectNames = jdbcTemplate.queryForStringList(query, objectType, name);
        List<String> dropStatements = new ArrayList<String>();
        for (String objectName : objectNames) {
            dropStatements.add("DROP " + objectType + " " + dbSupport.quote(name, objectName) + " " + extraArguments);
        }
        return dropStatements;
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
     * Generates the drop statements for scheduled jobs.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForScheduledJobs() throws SQLException {
        List<String> statements = new ArrayList<String>();

        List<String> jobNames = jdbcTemplate.queryForStringList("select JOB_NAME from ALL_SCHEDULER_JOBS WHERE owner=?", name);
        for (String jobName : jobNames) {
            statements.add("begin DBMS_SCHEDULER.DROP_JOB(job_name => '" + jobName + "', defer => false, force => true); end;");
        }

        return statements;
    }

    /**
     * Generates the drop statements for queue tables.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForQueueTables() throws SQLException {
        List<String> statements = new ArrayList<String>();

        List<String> queueTblNames = jdbcTemplate.queryForStringList("select QUEUE_TABLE from USER_QUEUE_TABLES");
        for (String queueTblName : queueTblNames) {
            statements.add("begin DBMS_AQADM.drop_queue_table (queue_table=> '" + queueTblName + "', FORCE => TRUE); end;");
        }

        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                // For every table this query will count the number of references (including the transitive ones)
                // and order the result list using that value.
                " SELECT r FROM" +
                        "   (SELECT CONNECT_BY_ROOT t r FROM" +
                        "     (SELECT DISTINCT c1.table_name f, NVL(c2.table_name, at.table_name) t" +
                        "     FROM all_constraints c1" +
                        "       RIGHT JOIN all_constraints c2 ON c2.constraint_name = c1.r_constraint_name" +
                        "       RIGHT JOIN all_tables at ON at.table_name = c2.table_name" +
                        "     WHERE at.owner = ?" +
                        // Ignore Recycle bin objects
                        "       AND at.table_name NOT LIKE 'BIN$%'" +
                        // Ignore Spatial Index Tables as they get dropped automatically when the index gets dropped.
                        "       AND at.table_name NOT LIKE 'MDRT_%$'" +
                        // Ignore Materialized View Logs
                        "       AND at.table_name NOT LIKE 'MLOG$%' AND at.table_name NOT LIKE 'RUPD$%'" +
                        // Ignore Oracle Text Index Tables
                        "       AND at.table_name NOT LIKE 'DR$%'" +
                        // Ignore Index Organized Tables
                        "       AND at.table_name NOT LIKE 'SYS_IOT_OVER_%'" +
                        // Ignore Nested Tables
                        "       AND at.nested != 'YES'" +
                        // Ignore Nested Tables
                        "       AND at.secondary != 'Y')" +
                        "   CONNECT BY NOCYCLE PRIOR f = t)" +
                        " GROUP BY r" +
                        " ORDER BY COUNT(*)", name);

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
     * Exposes jdbcTemplate to OracleSchemaObjectType
     */
    JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Exposes dbSupport to OracleSchemaObjectType
     */
    OracleDbSupport getDbSupport() {
        return dbSupport;
    }
}
