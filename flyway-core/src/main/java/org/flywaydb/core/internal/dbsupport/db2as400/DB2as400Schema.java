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
	private String systemSchema = "SYSCAT";
	
	// Field names
	private String schemaNameField = "SCHEMANAME";
	private String tableNameField = "TABNAME";
	private String sequenceNameField = "SEQNAME";
	private String procedureNameField = "PROCNAME";
	private String functionNameField = "FUNCNAME";
	private String specificNameField = "SPECIFICNAME";
	private String typeNameField = "TYPENAME";
	private String tableTypeField = "TYPE";
	private String tableSchemaField = "TABSCHEMA";
	private String viewSchemaField = "VIEWSCHEMA";
	private String sequenceSchemaField = "SEQSCHEMA";
	private String indexSchemaField = "INDSCHEMA";
	private String procedureSchemaField = "PROCSCHEMA";
	private String functionSchemaField = "FUNCSCHEMA";
	private String routineSchemaField = "ROUTSCHEMA";
	
	// Table-type values in system tables
	private String sequenceType = "S";
	private String tableType = "T";
	private String viewType = "V";
	private String aliasType = "A";
	private String parameterType = "P";
	
    /**
     * Creates a new DB2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public DB2as400Schema(JdbcTemplate jdbcTemplate, DB2as400DbSupport dbSupport, String name) {
        
        super(jdbcTemplate, dbSupport, name);
        
        	systemSchema = "SYSIBM";
        	
        	schemaNameField = "SCHEMA_NAME";
        	tableNameField = "TABLE_NAME";
        	sequenceNameField = "SEQUENCE_NAME";
        	procedureNameField = "PROCEDURE_NAME";
        	functionNameField = "FUNCTION_NAME";
        	specificNameField = "SPECIFIC_NAME";
        	typeNameField = "TYPE_NAME";
        	tableTypeField = "TABLE_TYPE";
        	tableSchemaField = "TABLE_SCHEMA";
        	viewSchemaField = "VIEW_SCHEMA";
        	sequenceSchemaField = "SEQUENCE_SCHEMA";
        	indexSchemaField = "INDEX_SCHEMA";
        	procedureSchemaField = "PROCEDURE_SCHEMA";
        	functionSchemaField = "FUNCTION_SCHEMA";
        	routineSchemaField = "ROUTINE_SCHEMA";
        	
        	sequenceType = "SEQUENCE";
        	tableType = "BASE TABLE";
        	viewType = "VIEW";
        	aliasType = "ALIAS";
        	parameterType = "PARAM";
    }

    @Override
    protected boolean doExists() throws SQLException {
    	String query = "SELECT COUNT(*) FROM " + systemSchema + ".schemata WHERE " + schemaNameField + "=?";
        return jdbcTemplate.queryForInt(query, name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = 0;
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + ".tables WHERE " + tableSchemaField + " = ?", name);
        //objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + ".views WHERE " + viewSchemaField + " = ?", name);
        //objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + ".sqlprocedures WHERE " + procedureSchemaField + " = ?", name);
        //objectCount += jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + systemSchema + ".sqlfunctions WHERE " + functionSchemaField + " = ?", name);
        return objectCount == 0;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
    	String query = "select rtrim(" + tableNameField + ") from " + systemSchema + ".TABLES where " + tableTypeField + "='" + tableType + "' and " + tableSchemaField + " = ?";
        return findTables(query, name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
    	String query = "select p." + specificNameField + ", p." + functionNameField + "," +
                " substr( xmlserialize( xmlagg( xmltext( concat( ', ', " + typeNameField + " ) ) ) as varchar( 1024 ) ), 3 ) as PARAMS" +
                " from " + systemSchema + ".FUNCTIONS f inner join " + systemSchema + ".FUNCPARMS p on f." + specificNameField + " = p." + specificNameField +
                " where f.ORIGIN = 'Q' and p." + functionSchemaField + " = ? and p.ROWTYPE = '" + parameterType + "'" +
                " group by p." + specificNameField + ", p."+ functionNameField +
                " order by p." + specificNameField;
    	
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
        //for (String dropStatement : generateDropStatements(name, viewType, "VIEW")) {
        //    System.out.println("Drop VIEW : " + dropStatement);
        //    jdbcTemplate.execute(dropStatement);
        //}

        // aliases
        for (String dropStatement : generateDropStatements(name, aliasType, "ALIAS")) {
            System.out.println("Drop ALIAS : " + dropStatement);
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
        	System.out.println("cleanTable : " + table.getName());
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
        String dropProcGenQuery = "select rtrim(" + procedureNameField + ") from " + systemSchema + ".PROCEDURES where " + procedureSchemaField + " = '" + schema + "'";
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
        String dropSeqGenQuery = "select rtrim(" + sequenceNameField + ") from " + systemSchema + ".SEQUENCES where " + sequenceSchemaField + " = '" + schema
                + "' and SEQTYPE='" + sequenceType + "'";
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
        String dropTablesGenQuery = "select rtrim(" + tableNameField + ") from " + systemSchema + ".TABLES where " + tableTypeField + "='" + tableType + "' and " + tableSchemaField + " = '"
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
