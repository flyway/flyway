/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.internal.dbsupport.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * DB2 implementation of Schema.
 */
public class DB2Schema extends Schema {
    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public DB2Schema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM syscat.schemata WHERE schemaname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("select count(*) from syscat.tables where tabschema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.views where viewschema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.sequences where seqschema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.indexes where indschema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.procedures where procschema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.functions where funcschema = ?", name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " RESTRICT");
    }

    @Override
    protected void doClean() throws SQLException {
        // MQTs are dropped when the backing views or tables are dropped
        // Indexes in DB2 are dropped when the corresponding table is dropped

        // drop versioned table link
        for (String dropVersioningStatement : generateDropVersioningStatement()) {
            jdbcTemplate.execute(dropVersioningStatement);
        }

        // views
        for (String dropStatement : generateDropStatements(name, "V", "VIEW")) {
            jdbcTemplate.execute(dropStatement);
        }

        // aliases
        for (String dropStatement : generateDropStatements(name, "A", "ALIAS")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        // sequences
        for (String dropStatement : generateDropStatementsForSequences(name)) {
            jdbcTemplate.execute(dropStatement);
        }

        // procedures
        for (String dropStatement : generateDropStatementsForProcedures(name)) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Function function : allFunctions()) {
            function.drop();
        }

        for (Type type : allTypes()) {
            type.drop();
        }
    }



    /**
     * Generates DROP statements for the procedures in this schema.
     *
     * @param schema The schema of the objects.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForProcedures(String schema) throws SQLException {
        String dropProcGenQuery = "select rtrim(PROCNAME) from SYSCAT.PROCEDURES where PROCSCHEMA = '" + schema + "'";
        return buildDropStatements("DROP PROCEDURE", dropProcGenQuery, schema);
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
        return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery, schema);
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
            dropStatements.add(dropPrefix + " " + dbSupport.quote(schema, dbObject));
        }
        return dropStatements;
    }

    /**
     * Returns all tables that have versioning associated with them.
     *
     * @return
     * @throws SQLException
     */
    private List<String> generateDropVersioningStatement() throws SQLException {
        List<String> dropVersioningStatements = new ArrayList<String>();
        Table[] versioningTables = findTables("select rtrim(TABNAME) from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", name);
        for(Table table : versioningTables) {
            dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
        }

        return dropVersioningStatements;
    }

    private Table[] findTables(String sqlQuery, String ... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2Table(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        return findTables("select rtrim(TABNAME) from SYSCAT.TABLES where TYPE='T' and TABSCHEMA = ?", name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        List<Map<String, String>> rows = jdbcTemplate.queryForList(
                "select p.SPECIFICNAME, p.FUNCNAME," +
                        " substr( xmlserialize( xmlagg( xmltext( concat( ', ', TYPENAME ) ) ) as varchar( 1024 ) ), 3 ) as PARAMS" +
                        " from SYSCAT.FUNCTIONS f inner join SYSCAT.FUNCPARMS p on f.SPECIFICNAME = p.SPECIFICNAME" +
                        " where f.ORIGIN = 'Q' and p.FUNCSCHEMA = ? and p.ROWTYPE = 'P'" +
                        " group by p.SPECIFICNAME, p.FUNCNAME" +
                        " order by p.SPECIFICNAME", name);

        List<Function> functions = new ArrayList<Function>();
        for (Map<String, String> row : rows) {
            functions.add(getFunction(
                    row.get("FUNCNAME"),
                    StringUtils.tokenizeToStringArray(row.get("PARAMS"), ",")));
        }

        return functions.toArray(new Function[functions.size()]);
    }

    @Override
    public Table getTable(String tableName) {
        return new DB2Table(jdbcTemplate, dbSupport, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new DB2Type(jdbcTemplate, dbSupport, this, typeName);
    }

    @Override
    public Function getFunction(String functionName, String... args) {
        return new DB2Function(jdbcTemplate, dbSupport, this, functionName, args);
    }
}
