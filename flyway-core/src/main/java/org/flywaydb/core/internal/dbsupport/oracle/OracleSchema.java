/**
 * Copyright 2010-2015 Axel Fontaine
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

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_users WHERE username=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(*) FROM all_objects WHERE owner = ?", name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE USER " + dbSupport.quote(name) + " IDENTIFIED BY flyway");
        jdbcTemplate.execute("GRANT RESOURCE TO " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP USER " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        if ("SYSTEM".equals(name.toUpperCase())) {
            throw new FlywayException("Clean not supported on Oracle for user 'SYSTEM'! You should NEVER add your own objects to the SYSTEM schema!");
        }

        jdbcTemplate.execute("PURGE RECYCLEBIN");

        for (String statement : generateDropStatementsForSpatialExtensions()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForQueueTables()) {
            //for dropping queue tables, a special grant is required:
            //GRANT EXECUTE ON DBMS_AQADM TO flyway;
            jdbcTemplate.execute(statement);
        }

        if (flashbackAvailable()) {
            executeAlterStatementsForFlashbackTables();
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
    }

    /**
     * Executes ALTER statements for all tables that have Flashback enabled.
     * Flashback is an asynchronous process so we need to wait until it completes, otherwise cleaning the
     * tables in schema will sometimes fail with ORA-55622 or ORA-55610 depending on the race between
     * Flashback and Java code
     *
     * @throws SQLException when the statements could not be generated.
     */
    private void executeAlterStatementsForFlashbackTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("SELECT table_name " +
                "FROM DBA_FLASHBACK_ARCHIVE_TABLES WHERE owner_name = ?", name);
        for (String tableName : tableNames) {
            jdbcTemplate.execute("ALTER TABLE " + dbSupport.quote(name, tableName) + " NO FLASHBACK ARCHIVE");
            String queryForOracleTechnicalTables = "SELECT count(archive_table_name) " +
                    "FROM user_flashback_archive_tables " +
                    "WHERE table_name = ?";
            //wait until the tables disappear
            while (jdbcTemplate.queryForInt(queryForOracleTechnicalTables, tableName) > 0) {
                try {
                    LOG.debug("Actively waiting for Flashback cleanup on table: " + tableName);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback cleanup interrupted", e);
                }
            }
        }
    }

    /**
     * Checks whether Oracle DBA_FLASHBACK_ARCHIVE_TABLES are available or not.
     *
     * @return {@code true} if they are available, {@code false} if not.
     * @throws SQLException when checking availability of the feature failed.
     */
    private boolean flashbackAvailable() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) " +
                "from all_objects " +
                "where object_name like 'DBA_FLASHBACK_ARCHIVE_TABLES'") > 0;
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
        return (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_users WHERE username = 'XDB'") > 0)
                && (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE view_name = 'RESOURCE_VIEW'") > 0);
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
                + " AND object_name NOT LIKE 'MDRS_%$'";

        List<String> objectNames = jdbcTemplate.queryForStringList(query, objectType, name);
        List<String> dropStatements = new ArrayList<String>();
        for (String objectName : objectNames) {
            dropStatements.add("DROP " + objectType + " " + dbSupport.quote(name, objectName) + " " + extraArguments);
        }
        return dropStatements;
    }

    /**
     * Generates the drop statements for Oracle Spatial Extensions-related database objects.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForSpatialExtensions() throws SQLException {
        List<String> statements = new ArrayList<String>();

        if (!spatialExtensionsAvailable()) {
            LOG.debug("Oracle Spatial Extensions are not available. No cleaning of MDSYS tables and views.");
            return statements;
        }
        if (!dbSupport.getCurrentSchema().getName().equalsIgnoreCase(name)) {
            int count = jdbcTemplate.queryForInt("SELECT COUNT (*) FROM all_sdo_geom_metadata WHERE owner=?", name);
            count += jdbcTemplate.queryForInt("SELECT COUNT (*) FROM all_sdo_index_info WHERE sdo_index_owner=?", name);
            if (count > 0) {
                LOG.warn("Unable to clean Oracle Spatial objects for schema '" + name + "' as they do not belong to the default schema for this connection!");
            }
            return statements;
        }


        statements.add("DELETE FROM mdsys.user_sdo_geom_metadata");

        List<String> indexNames = jdbcTemplate.queryForStringList("select INDEX_NAME from USER_SDO_INDEX_INFO");
        for (String indexName : indexNames) {
            statements.add("DROP INDEX \"" + indexName + "\"");
        }

        return statements;
    }

    /**
     * Generates the drop statements for scheduled jobs.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForScheduledJobs() throws SQLException {
        List<String> statements = new ArrayList<String>();

        List<String> jobNames = jdbcTemplate.queryForStringList("select JOB_NAME from USER_SCHEDULER_JOBS");
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

    /**
     * Checks whether Oracle Spatial extensions are available or not.
     *
     * @return {@code true} if they are available, {@code false} if not.
     * @throws SQLException when checking availability of the spatial extensions failed.
     */
    private boolean spatialExtensionsAvailable() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE owner = 'MDSYS' AND view_name = 'USER_SDO_GEOM_METADATA'") > 0;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM all_tables WHERE owner = ?"
                        // Ignore Recycle bin objects
                        + " AND table_name NOT LIKE 'BIN$%'"
                        // Ignore Spatial Index Tables as they get dropped automatically when the index gets dropped.
                        + " AND table_name NOT LIKE 'MDRT_%$'"
                        // Ignore Materialized View Logs
                        + " AND table_name NOT LIKE 'MLOG$%' AND table_name NOT LIKE 'RUPD$%'"
                        // Ignore Oracle Text Index Tables
                        + " AND table_name NOT LIKE 'DR$%'"
                        // Ignore Index Organized Tables
                        + " AND table_name NOT LIKE 'SYS_IOT_OVER_%'"
                        // Ignore Nested Tables
                        + " AND nested != 'YES'"
                        // Ignore Nested Tables
                        + " AND secondary != 'Y'", name
        );

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
