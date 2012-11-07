/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.db2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import com.googlecode.flyway.core.migration.sql.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB2 Support.
 */
public class DB2DbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DB2DbSupport(Connection connection) {
        super(new DB2JdbcTemplate(connection));
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DB2SqlStatementBuilder();
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        String upperCaseSchema = schema.toUpperCase();

        // TODO PROCEDURES and FUNCTIONS
        final List<String> allDropStatements = new ArrayList<String>();

        // MQTs are dropped when the backing views or tables are dropped
        // Indexes in DB2 are dropped when the corresponding table is dropped

        // views
        allDropStatements.addAll(generateDropStatements(upperCaseSchema, "V", "VIEW"));

        // aliases
        allDropStatements.addAll(generateDropStatements(upperCaseSchema, "A", "ALIAS"));

        // tables
        allDropStatements.addAll(generateDropStatements(upperCaseSchema, "T", "TABLE"));

        // sequences
        allDropStatements.addAll(generateDropStatementsForSequences(upperCaseSchema));


        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int count = 0;
        for (String dropStatement : allDropStatements) {
            count++;
            sqlStatements.add(new SqlStatement(count, dropStatement));
        }

        return new SqlScript(sqlStatements, this);
    }

    /**
     * Generates DROP statements for the sequences in this schema.
     *
     * @param schema The schema of the objects.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences(String schema) throws SQLException {
        String dropSeqGenQuery = "select rtrim(SEQNAME) from SYSCAT.SEQUENCES where SEQSCHEMA = '" + schema
                + "' and SEQTYPE='S'";
        return buildDropStatements("drop sequence", dropSeqGenQuery, schema);
    }

    /**
     * Generates DROP statements for this type of table, representing this type of object in this schema.
     *
     * @param schema     The schema of the objects.
     * @param tableType  The type of table (Can be T, V, S, ...).
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String schema, String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "select rtrim(TABNAME) from SYSCAT.TABLES where TYPE='" + tableType + "' and TABSCHEMA = '"
                + schema + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery, schema);
    }

    /**
     * Builds the drop statements for database objects in this schema.
     *
     * @param dropPrefix The drop command for the database object (e.g. 'drop table').
     * @param query      The query to get all present database objects
     * @param schema     The schema for which to build the statements.
     * @return The statements.
     * @throws SQLException when the drop statements could not be built.
     */
    private List<String> buildDropStatements(final String dropPrefix, final String query, String schema) throws SQLException {
        List<String> dropStatements = new ArrayList<String>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(query);
        for (String dbObject : dbObjects) {
            // DB2 needs double quotes
            dropStatements.add(dropPrefix + " \"" + schema + "\".\"" + dbObject + "\"");
        }
        return dropStatements;
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/db2/";
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("select count(*) from syscat.tables where tabschema = ?", schema);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.views where viewschema = ?", schema);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.sequences where seqschema = ?", schema);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.indexes where indschema = ?", schema);
        return objectCount == 0;
    }

    public boolean tableExists(String schema, String table) throws SQLException {
        return jdbcTemplate.tableExists(null, schema.toUpperCase(), table.toUpperCase());
    }

    public boolean columnExists(String schema, String table, String column) throws SQLException {
        return jdbcTemplate.columnExists(null, schema, table, column);
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1").trim();
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.update("lock table " + schema + "." + table + " in exclusive mode");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String quote(String identifier) {
        return "\"" + identifier + "\"";
    }
}
