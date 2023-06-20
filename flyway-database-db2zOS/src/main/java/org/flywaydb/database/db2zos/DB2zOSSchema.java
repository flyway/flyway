/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.database.db2zos;

import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DB2 implementation of Schema.
 */
public class DB2zOSSchema extends Schema<DB2zOSDatabase, DB2zOSTable> {
    /**
     * The tablespace where the schema history table resides.
     */
    private final String tablespace;

    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param name The name of the schema.
     * @param tablespace The tablespace where the schema history table resides.
     */
    DB2zOSSchema(JdbcTemplate jdbcTemplate, DB2zOSDatabase database, String name, String tablespace) {
        super(jdbcTemplate, database, name);
        this.tablespace = tablespace;
    }

    @Override
    protected boolean doExists() throws SQLException {
        return true;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) from (select distinct SCHEMA\n" +
                                                "from (select creator as SCHEMA from sysibm.systables\n" +
                                                "         union all\n" +
                                                "      select SCHEMA from sysibm.sysdatatypes\n" +
                                                "         union all\n" +
                                                "      select SCHEMA from sysibm.sysroutines\n" +
                                                "         union all\n" +
                                                "      select SCHEMA from sysibm.systriggers\n" +
                                                "     ) a WHERE SCHEMA = ?)", name) == 0;
    }

    @Override
    protected void doCreate() {
        // No CREATE SCHEMA necessary as schemas are dynamic in DB2 z/OS
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        // No DROP SCHEMA necessary as schemas are dynamic in DB2 z/OS
    }

    @Override
    protected void doClean() throws SQLException {
        // MQTs are dropped when the backing views or tables are dropped
        // Indexes in DB2 are dropped when the corresponding table is dropped

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

        // triggers
        for (String dropStatement : generateDropStatementsForTablespaces()) {
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
        String dropProcGenQuery = "select SPECIFICNAME from SYSIBM.SYSROUTINES where"
                // Stored procedures only
                + " ROUTINETYPE='P'"
                + " and SCHEMA = '" + name + "'";
        return buildDropStatements("DROP PROCEDURE", dropProcGenQuery);
    }

    /**
     * Generates DROP statements for the triggers in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTriggers() throws SQLException {
        String dropTrigGenQuery = "select NAME from SYSIBM.SYSTRIGGERS where SCHEMA = '" + name + "'";
        return buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
    }

    /**
     * Generates DROP statements for the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        String dropSeqGenQuery = "select NAME from SYSIBM.SYSSEQUENCES where SCHEMA = '" + name
                // User-defined sequences only
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
        String dropSeqGenQuery = "select NAME from sysibm.systables where CREATOR = '" + name
                + "' and TYPE='V'";
        return buildDropStatements("DROP VIEW", dropSeqGenQuery);
    }

    /**
     * Generates DROP statements for the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTablespaces() throws SQLException {
        List<String> dropStatements = new ArrayList<>();
        Set<String> tablespaces = new HashSet<>(jdbcTemplate.queryForStringList(
                "select concat(DBNAME, concat('.', TSNAME)) from sysibm.systables where TYPE='T' and CREATOR = '" + name + "'"));
        tablespaces.remove(tablespace);

        for (String ts : tablespaces) {
            dropStatements.add("DROP TABLESPACE " + ts);
        }
        return dropStatements;
    }

    /**
     * Generates DROP statements for this type of table, representing this type of object in this schema.
     *
     * @param tableType The type of table (Can be T, V, S, ...).
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "select NAME from sysibm.systables where TYPE='" + tableType + "' and CREATOR = '"
                + name + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery);
    }

    /**
     * Builds the drop statements for database objects in this schema.
     *
     * @param dropPrefix The drop command for the database object (e.g. 'drop table').
     * @param query The query to get all present database objects
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
        Table[] versioningTables = findTables("select NAME from sysibm.systables where VERSIONING_TABLE <> '' and CREATOR = ?", name);
        for (Table table : versioningTables) {
            dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
        }

        return dropVersioningStatements;
    }

    private DB2zOSTable[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        DB2zOSTable[] tables = new DB2zOSTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2zOSTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected DB2zOSTable[] doAllTables() throws SQLException {
        return findTables("select NAME from sysibm.systables where TYPE='T' and CREATOR = ?", name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        List<String> functionNames = jdbcTemplate.queryForStringList("select SPECIFICNAME from SYSIBM.SYSROUTINES where"
                                                                             // Non-system functions only
                                                                             + " ROUTINETYPE='F' AND ORIGIN <> 'S'"
                                                                             + " and SCHEMA = ?", name);

        List<Function> functions = new ArrayList<>();
        for (String functionName : functionNames) {
            functions.add(getFunction(functionName));
        }

        return functions.toArray(new Function[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new DB2zOSTable(jdbcTemplate, database, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new DB2zOSType(jdbcTemplate, database, this, typeName);
    }

    @Override
    public Function getFunction(String functionName, String... args) {
        return new DB2zOSFunction(jdbcTemplate, database, this, functionName, args);
    }
}