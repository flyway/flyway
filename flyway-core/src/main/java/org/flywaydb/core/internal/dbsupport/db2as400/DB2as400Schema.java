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
		
	// Table names
	private String schemaTable = "SYSIBM.SCHEMATA";					
	private String proceduresTable = "SYSIBM.SQLPROCEDURES";
	private String functionsTable = "QSYS2.SYSFUNCS";
	private String parametersTable = "QSYS2.SYSPARMS";
	private String tablesTable = "QSYS2.SYSTABLES";
	private String viewsTable = "SYSIBM.VIEWS";
	private String sequencesTable = "QSYS2.SYSSEQUENCES";
	
	// Field names
	private String dataTypeField = "DATA_TYPE";
	private String functionNameField = "ROUTINE_NAME";
	private String functionOriginField = "FUNCTION_ORIGIN";
	private String functionSchemaField = "FUNCTION_SCHEMA";		
	private String functionSchemField = "FUNCTION_SCHEM";		// NO 'A' at the end
	private String indexSchemaField = "INDEX_SCHEMA";
	private String procedureNameField = "PROCEDURE_NAME";
	private String procedureSchemField = "PROCEDURE_SCHEM";	// NO 'A' at the end
	private String rowTypeField = "ROW_TYPE";
	private String schemaNameField = "SCHEMA_NAME";
	private String sequenceNameField = "SEQUENCE_NAME";
	private String sequenceSchemaField = "SEQUENCE_SCHEMA";
	private String specificSchemaField = "SPECIFIC_SCHEMA";	
	private String specificNameField = "SPECIFIC_NAME";
	private String systemTableField = "SYSTEM_TABLE";
	private String tableNameField = "TABLE_NAME";
	private String tableTypeField = "TABLE_TYPE";
	private String tableSchemaField = "TABLE_SCHEMA";
	private String viewSchemaField = "VIEW_SCHEMA";
	private String routineSchemaField = "ROUTINE_SCHEMA";
	
	// Table-type values in system tables
	private String aliasValue = "A";
	private String parameterValue = "P";
	private String sequenceValue = "S";
	private String tableValue = "T";
	private String viewValue = "V";			
	private String yesValue = "Y";
	private String noValue = "N";

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
    	String query = "SELECT COUNT(*) FROM " + schemaTable + " WHERE " + schemaNameField + "=?";
        return jdbcTemplate.queryForInt(query, name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = 0;
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + tablesTable + " WHERE " + tableSchemaField + " = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + viewsTable + " WHERE " + viewSchemaField + " = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + proceduresTable + " WHERE " + procedureSchemField + " = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + functionsTable + " WHERE " + functionSchemField + " = ?", name);
        return objectCount == 0;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
    	String query = "SELECT RTRIM(" + tableNameField + ") FROM " + tablesTable + " WHERE " + tableTypeField + "='" + tableValue + "' AND " + tableSchemaField + " = ?";
        return findTables(query, name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
    	String query = "SELECT f." + specificNameField + ", f." + functionNameField + "," +
                " SUBSTR(xmlserialize(xmlagg(xmltext(CONCAT(', ', " + dataTypeField + "))) AS VARCHAR(1024)), 3) AS PARAMS" +
                " FROM " + functionsTable + " f INNER JOIN " + parametersTable + " p ON f." + specificNameField + " = p." + specificNameField +
                " WHERE f." + functionOriginField + "='Q' " + 
                " AND p." + specificSchemaField + "=? " + 
                " AND p." + rowTypeField + "='" + parameterValue + "'" +
                " GROUP BY f." + specificNameField + ", f."+ functionNameField +
                " ORDER BY f." + specificNameField;
    	
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

    	// views
        for (String dropStatement : generateDropStatements(name, viewValue, "VIEW")) {
            jdbcTemplate.execute(dropStatement);
        }

        // aliases
        for (String dropStatement : generateDropStatements(name, aliasValue, "ALIAS")) {
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
        String dropProcGenQuery = "SELECT RTRIM(" + procedureNameField + ") FROM " + proceduresTable + " WHERE " + procedureSchemField + " = '" + schema + "'";
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
        String dropSeqGenQuery = "SELECT RTRIM(" + sequenceNameField + ") FROM " + sequencesTable 
        		+ " WHERE " + sequenceSchemaField + " ='" + schema + "'";
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
    	String dropTablesGenQuery = "SELECT RTRIM(" + tableNameField + ") FROM " + tablesTable + 
        		" WHERE " + tableTypeField + "='" + tableType + "'" +
        		" AND " + tableSchemaField + "='" + schema + "'";
    	
    	if ("VIEW".equalsIgnoreCase(objectType)) {
    		// Drop only non system views
    		dropTablesGenQuery += " AND " + systemTableField + "='" + noValue + "'";
    	}
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
