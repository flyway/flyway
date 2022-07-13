/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.db2z;

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
public class DB2ZSchema extends Schema<DB2ZDatabase, DB2ZTable> {
    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    DB2ZSchema(JdbcTemplate jdbcTemplate, DB2ZDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
		/**
		* For DB2 on z/OS, a schema is not an object that can be created or dropped and is not listed in the catalog. 
		* Instead, we do need to check whether the database exists (which is a container for tablespaces and other storage related objects)
		*/
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM sysibm.sysdatabase WHERE name=?", database.getName()) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("select count(*) from sysibm.systables where dbname = ? AND creator = ?", database.getName(), name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sysibm.syssequences where schema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sysibm.sysindexes where creator = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sysibm.sysroutines where schema = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sysibm.systriggers where schema = ?", name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        throw new UnsupportedOperationException("Create Schema - is not supported in db2 on zOS");
    }

    @Override
    protected void doDrop() throws SQLException {
        throw new UnsupportedOperationException("Drop Schema - is not supported in db2 on zOS");
    }

    @Override
    protected void doClean() throws SQLException {
        // MQTs are dropped when the backing views or tables are dropped
        // Indexes in DB2 are dropped when the corresponding table is dropped

             // drop versioned table link -> not supported for DB2 9.x
            List<String> dropVersioningStatements = generateDropVersioningStatement();
            if (!dropVersioningStatements.isEmpty()) {
                // Do a explicit drop of MQTs in order to be able to drop the Versioning
                for (String dropTableStatement : generateDropStatements("M", "TABLE")) {
                    jdbcTemplate.execute(dropTableStatement);
                }
            }

            for (String dropVersioningStatement : dropVersioningStatements) {
                jdbcTemplate.execute(dropVersioningStatement);
            }

        // views
        /* We need to query for all views in schema after each DROP because of a
         * specific property in z/OS to DROP dependent nested views (views depending on other views).
         */
        List<String> dropStatements = generateDropStatements("V", "VIEW");
        while(dropStatements.size() != 0) {
           	String dropStatement = dropStatements.get(0);
           	jdbcTemplate.execute(dropStatement);
         	dropStatements = generateDropStatements("V", "VIEW");
        }

        // aliases
        for (String dropStatement : generateDropStatements("A", "ALIAS")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        // temporary Tables
        for (String dropStatement : generateDropStatements("G", "TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        // explicit tablespace
        for (String dropStatement : generateDropStatementsForRegularTablespace()) {
            jdbcTemplate.execute(dropStatement);
        }
        
        for (String dropStatement : generateDropStatementsForLobTablespace()) {
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
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForProcedures() throws SQLException {
        String dropProcGenQuery = "select rtrim(NAME) from SYSIBM.SYSROUTINES where CAST_FUNCTION = 'N' " +
                " and ROUTINETYPE  = 'P' and SCHEMA = '" + name + "'";
        return buildDropStatements("DROP PROCEDURE", dropProcGenQuery);
    }

    /**
     * Generates DROP statements for the sequences in this schema.
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        String dropSeqGenQuery = "select rtrim(NAME) from SYSIBM.SYSSEQUENCES where SCHEMA = '" + name
                + "' and SEQTYPE='S'";
        return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery);
    }

    /**
     * Generates DROP statements for the explicitly-created tablespaces in this database with the schema user as creator.
     *
     * @return The drop statements.
     * @throws java.sql.SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForRegularTablespace() throws SQLException {
		//Only drop explicitly created tablespaces for current database and created under this specific schema authorization ID
		//Note that this also drops the related table for partitioned tablespaces.
        String dropTablespaceGenQuery = "select rtrim(NAME) FROM SYSIBM.SYSTABLESPACE where IMPLICIT = 'N' AND DBNAME = '" + database.getName() + "' AND CREATOR = '" + name + "' AND TYPE <> 'O'";

        List<String> dropStatements = new ArrayList<>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(dropTablespaceGenQuery);
        for (String dbObject : dbObjects) {
            dropStatements.add("DROP TABLESPACE " + database.quote(database.getName(), dbObject));
        }
        return dropStatements;
    }
    
    private List<String> generateDropStatementsForLobTablespace() throws SQLException {
    	String dropTablespaceGenQuery = "select rtrim(NAME) FROM SYSIBM.SYSTABLESPACE where IMPLICIT = 'N' AND DBNAME = '" + database.getName() + "' AND CREATOR = '" + name + "' AND TYPE = 'O'";

        List<String> dropStatements = new ArrayList<>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(dropTablespaceGenQuery);
        for (String dbObject : dbObjects) {
            dropStatements.add("DROP TABLESPACE " + database.quote(database.getName(), dbObject));
        }
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
    private List<String> generateDropStatements(String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "select rtrim(NAME) from SYSIBM.SYSTABLES where TYPE='" + tableType + "' and (DBNAME = '" + database.getName() + "' AND CREATOR = '" + name + "')";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery);
    }

    /**
     * Generates DROP statements for the triggers in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForTriggers() throws SQLException {
        String dropTrigGenQuery = "select TRIGNAME from SYSIBM.SYSTRIGGERS where SCHEMA = '" + name + "'";
        return buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
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
        Table[] versioningTables = findTables("select rtrim(NAME) from SYSIBM.SYSTABLES where VERSIONING_TABLE <> '' and DBNAME = '" + database.getName() + "' AND CREATOR = ?", name);
        for (Table table : versioningTables) {
            dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
        }

        return dropVersioningStatements;
    }

    private DB2ZTable[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        DB2ZTable[] tables = new DB2ZTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2ZTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected DB2ZTable[] doAllTables() throws SQLException {
        return findTables("select rtrim(NAME) from SYSIBM.SYSTABLES where TYPE='T' and DBNAME = '" + database.getName() + "' AND CREATOR = ?", name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        List<String> functionNames = jdbcTemplate.queryForStringList(
                "select rtrim(SPECIFICNAME) from SYSIBM.SYSROUTINES where"
                        // Functions only
                        + " ROUTINETYPE='F'"
                        // That aren't system-generated or built-in
                        + " AND ORIGIN IN ("
                        + "'E', " // User-defined, external
                        + "'M', " // Template function
                        + "'Q', " // SQL-bodied
                        + "'U')"  // User-defined, based on a source
                        + " and SCHEMA = ?", name);

        List<Function> functions = new ArrayList<>();
        for (String functionName : functionNames) {
            functions.add(getFunction(functionName));
        }

        return functions.toArray(new Function[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new DB2ZTable(jdbcTemplate, database, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new DB2ZType(jdbcTemplate, database, this, typeName);
    }

    @Override
    public Function getFunction(String functionName, String... args) {
        return new DB2ZFunction(jdbcTemplate, database, this, functionName, args);
    }
}