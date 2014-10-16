/**
 * Copyright 2014 Bertrand DONNET
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

import org.flywaydb.core.internal.dbsupport.*;
import org.flywaydb.core.internal.dbsupport.db2.DB2Schema;
import org.flywaydb.core.internal.dbsupport.db2.DB2Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DB2/AS400 implementation of Schema.
 */
public class DB2as400Schema extends DB2Schema {
	
	// System schema name
	private String systemSchema = "SYSIBM";						// OK
	
	// Field names
	private String schemaNameField = "SCHEMA_NAME";				// OK
	private String tableNameField = "TABLE_NAME";				// OK
	private String sequenceNameField = "SEQUENCE_NAME";
	private String procedureNameField = "PROCEDURE_NAME";
	private String functionNameField = "FUNCTION_NAME";
	private String specificNameField = "SPECIFIC_NAME";
	private String typeNameField = "TYPE_NAME";
	private String tableTypeField = "TABLE_TYPE";
	private String tableSchemaField = "TABLE_SCHEMA";
	private String viewSchemaField = "VIEW_SCHEMA";
	private String sequenceSchemaField = "SEQUENCE_SCHEMA";
	private String indexSchemaField = "INDEX_SCHEMA";
	private String procedureSchemaField = "PROCEDURE_SCHEMA";
	private String functionSchemaField = "FUNCTION_SCHEMA";
	private String routineSchemaField = "ROUTINE_SCHEMA";
	
	// Table names
	private String schemaTable = "SCHEMATA";					// OK
	private String sqlProceduresTable = "SQLPROCEDURES";		// OK
	private String sqlFunctionsTable = "SQLFUNCTIONS";			// OK
	private String tablesTable = "TABLES";						// OK
	private String viewsTable = "VIEWS";						// OK

	// Table-type values in system tables
	private String sequenceType = "SEQUENCE";
	private String tableType = "BASE TABLE";
	private String viewType = "VIEW";			
	private String aliasType = "ALIAS";
	private String parameterType = "PARAM";
	
    /**
     * Creates a new DB2as400 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public DB2as400Schema(JdbcTemplate jdbcTemplate, DB2as400DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
    	String query = "SELECT COUNT(*) FROM " + systemSchema + "." + schemaTable + " WHERE " + schemaNameField + "=?";
        return jdbcTemplate.queryForInt(query, name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = 0;
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + "." + tablesTable + " WHERE " + tableSchemaField + " = ?", name);
        //objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + "." + viewsTable + " WHERE " + viewSchemaField + " = ?", name);
        //objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + "." + sqlProceduresTable + " WHERE " + procedureSchemaField + " = ?", name);
        //objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + "." + sqlFunctionsTable + " WHERE " + functionSchemaField + " = ?", name);
        return objectCount == 0;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
    	String query = "SELECT RTRIM(" + tableNameField + ") FROM " + systemSchema + "." + tablesTable + " WHERE " + tableTypeField + "='" + tableType + "' AND " + tableSchemaField + " = ?";
        return findTables(query, name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
    	String query = "SELECT p." + specificNameField + ", p." + functionNameField + "," +
                " SUBSTR( xmlserialize( xmlagg( xmltext( CONCAT( ', ', " + typeNameField + " ) ) ) AS varchar( 1024 ) ), 3 ) AS PARAMS" +
                " FROM " + systemSchema + ".FUNCTIONS f INNER JOIN " + systemSchema + ".FUNCPARMS p ON f." + specificNameField + " = p." + specificNameField +
                " WHERE f.ORIGIN = 'Q' AND p." + functionSchemaField + " = ? AND p.ROWTYPE = '" + parameterType + "'" +
                " GROUP BY p." + specificNameField + ", p."+ functionNameField +
                " ORDER BY p." + specificNameField;
    	
        List<Map<String, String>> rows = jdbcTemplate.queryForList(query, name);

        List<Function> functions = new ArrayList<Function>();
        for (Map<String, String> row : rows) {
            functions.add(getFunction(
                    row.get(functionNameField),
                    StringUtils.tokenizeToStringArray(row.get("PARAMS"), ",")));
        }

        return functions.toArray(new Function[functions.size()]);
    }
    
    @Override
    protected void doClean() throws SQLException {

    	// Commented because System views cannot be deleted
        // views
        //for (String dropStatement : generateDropStatements(name, viewType, "VIEW")) {
        //    jdbcTemplate.execute(dropStatement);
        //}

        // aliases
        for (String dropStatement : generateDropStatements(name, aliasType, "ALIAS")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
            table.drop();
        }
        
        // TODO: not yet rewritten for AS/400
        /*
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
        */
	    
    }

    /**
     * Generates DROP statements for the procedures in this schema.
     *
     * @param schema The schema of the objects.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForProcedures(String schema) throws SQLException {
        String dropProcGenQuery = "SELECT RTRIM(" + procedureNameField + ") FROM " + systemSchema + ".PROCEDURES where " + procedureSchemaField + " = '" + schema + "'";
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
        String dropSeqGenQuery = "SELECT RTRIM(" + sequenceNameField + ") FROM " + systemSchema + ".SEQUENCES WHERE " + sequenceSchemaField + " = '" + schema
                + "' AND SEQTYPE='" + sequenceType + "'";
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
        String dropTablesGenQuery = "SELECT RTRIM(" + tableNameField + ") FROM " + systemSchema + "." + tablesTable + " WHERE " + tableTypeField + "='" + tableType + "' AND " + tableSchemaField + " = '"
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

    private Table[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2Table(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }
}
