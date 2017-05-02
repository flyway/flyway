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
package org.flywaydb.core.internal.dbsupport.db2zos;

import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB2 implementation of Schema.
 */
public class DB2zosSchema extends Schema<DB2zosDbSupport> {

    private static final Log LOG = LogFactory.getLog(DB2zosSchema.class);

    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public DB2zosSchema(JdbcTemplate jdbcTemplate, DB2zosDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }


    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSIBM.SYSTABLES WHERE CREATOR=?", name) > 0;
    }


    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSIBM.SYSTABLES WHERE CREATOR = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSIBM.SYSSEQUENCES WHERE SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSIBM.SYSINDEXES WHERE CREATOR = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSIBM.SYSROUTINES WHERE SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSIBM.SYSDATATYPES WHERE SCHEMA = ?", name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        LOG.warn("Create Schema - is not supported in db2 on zOS");
    }

    @Override
    protected void doDrop() throws SQLException {
        LOG.warn("Drop Schema - is not supported in db2 on zOS, doing dbClean() instead");
        doClean();
    }

    @Override
    protected void doClean() throws SQLException {
        // MQTs are dropped when the backing views or tables are dropped
        // Indexes in DB2 are dropped when the corresponding table is dropped

        // views
        for (String dropStatement : generateDropStatements("V", "VIEW")) {
            jdbcTemplate.execute(dropStatement);
        }

        // aliases
        for (String dropStatement : generateDropStatements("A", "ALIAS")) {
            jdbcTemplate.execute(dropStatement);
        }

        // drop tables - tablespaces
        for (String dropStatement : generateDropStatementsForTablespace()) {
            jdbcTemplate.execute(dropStatement);
        }

        // drop tables which are not dropped before
        for (Table table : allTables()) {
            table.drop();
        }

        // slett testtabeller
        for (String dropStatement : generateDropStatementsForTestTable("T", "TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        // sequences
        for (String dropStatement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(dropStatement);
        }

        // procedures
        for (String dropStatement : generateDropStatementsForProcedures()) {
            jdbcTemplate.execute(dropStatement);
        }

        // functions
        for (String dropStatement : generateDropStatementsForFunctions()) {
            jdbcTemplate.execute(dropStatement);
        }

        // usertypes
        for (String dropStatement : generateDropStatementsForUserTypes()) {
            jdbcTemplate.execute(dropStatement);
        }
    }

    /**
     * Generates DROP statements for the procedures in this schema.
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForProcedures() throws SQLException {
        String dropProcGenQuery = "SELECT TRIM(NAME) " +
                                    "FROM SYSIBM.SYSROUTINES " +
                                   "WHERE CAST_FUNCTION = 'N' " +
                                     "AND ROUTINETYPE  = 'P' " +
                                     "AND SCHEMA = '" + name + "'";
        return buildDropStatements("DROP PROCEDURE", dropProcGenQuery, name);
    }

    /**
     * Generates DROP statements for the functions in this schema.
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForFunctions() throws SQLException {
        String dropProcGenQuery = "SELECT TRIM(NAME) " +
                                    "FROM SYSIBM.SYSROUTINES " +
                                   "WHERE CAST_FUNCTION = 'N' " +
                                     "AND ROUTINETYPE  = 'F' " + "" +
                                     "AND SCHEMA = '" + name + "'";
        return buildDropStatements("DROP FUNCTION", dropProcGenQuery, name);
    }

    /**
     * Generates DROP statements for the sequences in this schema.
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        String dropSeqGenQuery = "SELECT TRIM(NAME) " +
                                   "FROM SYSIBM.SYSSEQUENCES " +
                                  "WHERE SCHEMA = '" + name + "'" +
                                    "AND SEQTYPE='S'";
        return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery, name);
    }

    /**
     * Generates DROP statements for the tablespace in this schema.
     * drop tablespace <dbname>.<tablespacename>;
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTablespace() throws SQLException {
        List<String> dropStatements;

        // all tablespaces (with tables) with CREATOR = name
        // a tablespace can contain nothing, one or many tables
        String dropTablespaceGenQuery = "SELECT DISTINCT TRIM(DBNAME) || '.' || TRIM(TSNAME) " +
                                          "FROM SYSIBM.SYSTABLES T1 " +
                                         "WHERE CREATOR = '"  + name + "' " +
                                           "AND NOT EXISTS (SELECT 1 " +
                                                             "FROM SYSIBM.SYSTABLES T2 " +
                                                            "WHERE T2.TSNAME = T1.TSNAME " +
                                                              "AND T2.DBNAME = T1.DBNAME " +
                                                              "AND T2.CREATOR <> T1.CREATOR); ";
        dropStatements = generateDropTablespaces(dropTablespaceGenQuery);

        // all tablespaces (with no tables) with CREATOR = name
        String dropTablespaceWithoutTableGenQuery = "SELECT DISTINCT TRIM(DBNAME) || '.' || TRIM(NAME) " +
                                                      "FROM SYSIBM.SYSTABLESPACE T1 " +
                                                     "WHERE CREATOR = '"  + name + "' " +
                                                       "AND NOT EXISTS (SELECT 1 " +
                                                                         "FROM SYSIBM.SYSTABLES T2 " +
                                                                        "WHERE T2.TSNAME = T1.NAME " +
                                                                          "AND T2.DBNAME = T1.DBNAME " +
                                                                          "AND T2.CREATOR = T1.CREATOR); ";
        dropStatements.addAll(generateDropTablespaces(dropTablespaceWithoutTableGenQuery));

        return dropStatements;
    }

    /**
     * Generates DROP statements for this type of table, representing this type of object in this schema.
     *
     * @param tableType  The type of table (Can be T, V, S, ...).
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTestTable(String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "SELECT TRIM(NAME) " +
                                      "FROM SYSIBM.SYSTABLES " +
                                     "WHERE TYPE = '" + tableType + "'" +
                                       "AND CREATOR = '"  + name + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery, name);
    }

    /**
     * Generates DROP statements for the user defines types in this schema.
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForUserTypes() throws SQLException {
        String dropDataTypesGenQuery = "SELECT TRIM(NAME) " +
                                         "FROM SYSIBM.SYSDATATYPES " +
                                        "WHERE SCHEMA = '" + name + "'";
        return buildDropStatements("DROP TYPE", dropDataTypesGenQuery, name);
    }

    /**
     * Generates DROP statements for this type of table, representing this type of object in this schema.
     *
     * @param tableType  The type of table (Can be T, V, S, ...).
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "SELECT TRIM(NAME) " +
                                      "FROM SYSIBM.SYSTABLES " +
                                     "WHERE TYPE = '" + tableType + "'" +
                                       "AND CREATOR = '" + name + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery, name);
    }

    /**
     * Builds the drop statements for database objects in this schema.
     *
     * @param dropPrefix The drop command for the database object (e.g. 'drop table').
     * @param query      The query to get all present database objects
     * @param schema     The schema for which to build the statements.
     * @return The statements.
     * @throws java.sql.SQLException when the drop statements could not be built.
     */
    private List<String> buildDropStatements(final String dropPrefix, final String query, String schema) throws SQLException {
        List<String> dropStatements = new ArrayList<String>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(query);
        for (String dbObject : dbObjects) {
            dropStatements.add(dropPrefix + " " + dbSupport.quote(schema, dbObject) + ";");
        }
        return dropStatements;
    }

    /**
     * Builds the drop statements for tablespace objects in this schema.
     *
     * @param query      The query to get all present database objects
     * @return The statements.
     * @throws java.sql.SQLException when the drop statements could not be built.
     */
    private List<String> generateDropTablespaces(String query) throws SQLException {
        List<String> dropStatements = new ArrayList<String>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(query);
        for (String dbObject : dbObjects) {
            dropStatements.add("DROP TABLESPACE" + " " + dbObject + ";");
        }
        return dropStatements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "select trim(NAME) from SYSIBM.SYSTABLES where TYPE='T' and CREATOR = ?", name);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2zosTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new DB2zosTable(jdbcTemplate, dbSupport, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new DB2zosType(jdbcTemplate, dbSupport, this, typeName);
    }

    @Override
    public Function getFunction(String functionName, String... args) {
        return new DB2zosFunction(jdbcTemplate, dbSupport, this, functionName, args);
    }
}
