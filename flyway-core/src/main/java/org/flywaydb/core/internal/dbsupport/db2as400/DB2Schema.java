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
package org.flywaydb.core.internal.dbsupport.db2as400;

import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DB2 implementation of Schema.
 */
public class DB2Schema extends Schema<DB2AS400DbSupport> {
    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public DB2Schema(JdbcTemplate jdbcTemplate, DB2AS400DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM QSYS2.SYSSCHEMAS WHERE SCHEMA_NAME = ?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSTABLES where TABLE_SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSVIEWS where TABLE_SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSSEQ where SEQUENCE_SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSINDEXES where INDEX_SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSPROCS where SPECIFIC_SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSFUNCS where SPECIFIC_SCHEMA = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from QSYS2.SYSTRIGGER where TRIGGER_SCHEMA = ?", name);
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

        // views
        for (String dropStatement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(dropStatement);
        }

        // aliases
        for (String dropStatement : generateDropStatements("A", "ALIAS")) {
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
        String dropProcGenQuery = "select SPECIFIC_NAME from QSYS2.SYSPROCS where SPECIFIC_SCHEMA = '" + name + "'";

        return buildDropStatements("DROP PROCEDURE", dropProcGenQuery);
    }

    /**
     * Generates DROP statements for the triggers in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTriggers() throws SQLException {
        String dropTrigGenQuery = "select TRIGGER_NAME from QSYS2.SYSTRIGGER where TRIGGER_SCHEMA = '" + name + "'";
        return buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
    }

    /**
     * Generates DROP statements for the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        String dropSeqGenQuery = "select SEQUENCE_NAME from QSYS2.SYSSEQ where SEQUENCE_SCHEMA = '" + name + "'";

        return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery);
    }

    /**
     * Generates DROP statements for the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForViews() throws SQLException {
        String dropSeqGenQuery = "select TABLE_NAME from QSYS2.SYSVIEWS where TABLE_SCHEMA = '" + name + "'";
        return buildDropStatements("DROP VIEW", dropSeqGenQuery);
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
        String dropTablesGenQuery = "select TABLE_NAME from QSYS2.SYSTABLES where TABLE_SCHEMA = '" + name + "' and TABLE_TYPE = '" + tableType + "'";
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
        List<String> dropStatements = new ArrayList<String>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(query);
        for (String dbObject : dbObjects) {
            dropStatements.add(dropPrefix + " " + dbSupport.quote(name, dbObject));
        }
        return dropStatements;
    }

    private Table[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2Table(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        return findTables("select TABLE_NAME from QSYS2.SYSTABLES where TABLE_TYPE = 'T' and TABLE_SCHEMA = ?", name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        Map<String, List<String>> records = new LinkedHashMap<String, List<String>>();



        List<Map<String, String>> rows = jdbcTemplate.queryForList("select sp.SPECIFIC_NAME, sp.PARAMETER_NAME, sp.ORDINAL_POSITION " +
                        "from QSYS2.SYSFUNCS sf inner join QSYS2.SYSPARMS sp " +
                        "on sp.SPECIFIC_SCHEMA=sf.SPECIFIC_SCHEMA and sp.SPECIFIC_NAME=sf.SPECIFIC_NAME " +
                        "where sp.PARAMETER_MODE = 'IN' and sf.SPECIFIC_SCHEMA = ? " +
                        "order by sp.SPECIFIC_NAME, sp.PARAMETER_NAME, sp.ORDINAL_POSITION", name);

        for (Map<String, String> row : rows) {
            String fName = (String) row.get("SPECIFIC_NAME");
            String pName = (String) row.get("PARAMETER_NAME");
            List<String> params = records.get(fName);
            if(params == null) {
                params = new ArrayList<String>();
                records.put(fName, params);
            }
            params.add(pName);
        }
        List<Function> functions = new ArrayList<Function>();
        for(Map.Entry<String, List<String>> entry : records.entrySet()) {
            functions.add(getFunction(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()])));
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
