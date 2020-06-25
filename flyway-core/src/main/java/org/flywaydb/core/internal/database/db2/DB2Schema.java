/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB2 implementation of Schema.
 */
public class DB2Schema extends Schema<DB2Database, DB2Table> {
    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    DB2Schema(JdbcTemplate jdbcTemplate, DB2Database database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(*) from ("
                + "SELECT 1 FROM syscat.schemata WHERE schemaname=?"
                + ")", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) from ("
                + "select 1 from syscat.tables where tabschema = ? "
                + "union "
                + "select 1 from syscat.views where viewschema = ? "
                + "union "
                + "select 1 from syscat.sequences where seqschema = ? "
                + "union "
                + "select 1 from syscat.indexes where indschema = ? "
                + "union "
                + "select 1 from syscat.routines where ROUTINESCHEMA = ? "
                + "union "
                + "select 1 from syscat.triggers where trigschema = ? "
                + ")", name, name, name, name, name, name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " RESTRICT");
    }

    @Override
    protected void doClean() throws SQLException {
        // MQTs are dropped when the backing views or tables are dropped
        // Indexes in DB2 are dropped when the corresponding table is dropped




            // drop versioned table link -> not supported for DB2 9.x
            List<String> dropVersioningStatements = generateDropVersioningStatement();
            if (!dropVersioningStatements.isEmpty()) {
                // Do a explicit drop of MQTs in order to be able to drop the Versioning
                for (String dropTableStatement : generateDropStatements("S", "TABLE")) {
                    jdbcTemplate.execute(dropTableStatement);
                }
            }

            for (String dropVersioningStatement : dropVersioningStatements) {
                jdbcTemplate.execute(dropVersioningStatement);
            }




        // views
        for (String dropStatement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(dropStatement);
        }

        // aliases
        for (String dropStatement : generateDropStatements("A", "ALIAS")) {
            jdbcTemplate.execute(dropStatement);
        }

        // temporary Tables
        for (String dropStatement : generateDropStatements("G", "TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        // sequences
        for (String dropStatement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(dropStatement);
        }

        // procedures
        for (String dropStatement : generateDropStatementsForProcedures()) {
            jdbcTemplate.execute(dropStatement);
        }

        // triggers
        for (String dropStatement : generateDropStatementsForTriggers()) {
            jdbcTemplate.execute(dropStatement);
        }

        // modules
        for (String dropStatement : generateDropStatementsForModules()) {
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
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForProcedures() throws SQLException {
        String dropProcGenQuery =
                "select SPECIFICNAME from SYSCAT.ROUTINES where ROUTINETYPE='P' and ROUTINESCHEMA = '" + name + "'" + " and ROUTINEMODULENAME IS NULL";
        return buildDropStatements("DROP SPECIFIC PROCEDURE", dropProcGenQuery);
    }

    /**
     * Generates DROP statements for the triggers in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTriggers() throws SQLException {
        String dropTrigGenQuery = "select TRIGNAME from SYSCAT.TRIGGERS where TRIGSCHEMA = '" + name + "'";
        return buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
    }

    /**
     * Generates DROP statements for the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        String dropSeqGenQuery = "select SEQNAME from SYSCAT.SEQUENCES where SEQSCHEMA = '" + name
                + "' and SEQTYPE='S'";
        return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery);
    }

    /**
     * Generates DROP statements for the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForViews() throws SQLException {
        String dropSeqGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='V' AND TABSCHEMA = '" + name + "'" +




                        // Filter out statistical view for an index with an expression-based key
                        // See https://www.ibm.com/support/knowledgecenter/SSEPGG_10.5.0/com.ibm.db2.luw.sql.ref.doc/doc/r0001063.html
                        " and substr(property,19,1) <> 'Y'"



                ;

        return buildDropStatements("DROP VIEW", dropSeqGenQuery);
    }

    private List<String> generateDropStatementsForModules() throws SQLException {
        String dropSeqGenQuery =
                "select MODULENAME from syscat.modules where MODULESCHEMA = '"
                + name
                + "' and OWNERTYPE='U'";


        return buildDropStatements("DROP MODULE", dropSeqGenQuery);
    }

    /**
     * Generates DROP statements for this type of table, representing this type of object in this schema.
     *
     * @param tableType  The type of table (Can be T, V, S, ...).
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='" + tableType + "' and TABSCHEMA = '"
                + name + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery);
    }

    /**
     * Builds the drop statements for database objects in this schema.
     *
     * @param dropPrefix The drop command for the database object (e.g. 'drop table').
     * @param query      The query to get all present database objects
     * @return The statements.
     * @throws SQLException when the drop statements could not be built.
     */
    private List<String> buildDropStatements(final String dropPrefix, final String query) throws SQLException {
        List<String> dropStatements = new ArrayList<>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(query);
        for (String dbObject : dbObjects) {
            dropStatements.add(dropPrefix + " " + database.quote(name, dbObject));
        }
        return dropStatements;
    }

    /**
     * @return All tables that have versioning associated with them.
     */
    private List<String> generateDropVersioningStatement() throws SQLException {
        List<String> dropVersioningStatements = new ArrayList<>();
        Table[] versioningTables = findTables("select TABNAME from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", name);
        for (Table table : versioningTables) {
            dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
        }

        return dropVersioningStatements;
    }

    private DB2Table[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        DB2Table[] tables = new DB2Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2Table(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected DB2Table[] doAllTables() throws SQLException {
        return findTables("select TABNAME from SYSCAT.TABLES where TYPE='T' and TABSCHEMA = ?", name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        List<String> functionNames = jdbcTemplate.queryForStringList(
                "select SPECIFICNAME from SYSCAT.ROUTINES where"
                        // Functions only
                        + " ROUTINETYPE='F'"
                        // That aren't system-generated or built-in
                        + " AND ORIGIN IN ("
                        + "'E', " // User-defined, external
                        + "'M', " // Template function
                        + "'Q', " // SQL-bodied
                        + "'U')"  // User-defined, based on a source
                        + " and ROUTINESCHEMA = ?", name);

        List<Function> functions = new ArrayList<>();
        for (String functionName : functionNames) {
            functions.add(getFunction(functionName));
        }

        return functions.toArray(new Function[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new DB2Table(jdbcTemplate, database, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new DB2Type(jdbcTemplate, database, this, typeName);
    }

    @Override
    public Function getFunction(String functionName, String... args) {
        return new DB2Function(jdbcTemplate, database, this, functionName, args);
    }
}