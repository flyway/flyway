/**
 * Copyright 2010-2014 Axel Fontaine
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.flywaydb.core.internal.dbsupport.db2as400;

import org.flywaydb.core.internal.dbsupport.*;
import org.flywaydb.core.internal.dbsupport.db2.DB2Schema;
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
    private static final String schemaTable = "SYSIBM.SCHEMATA";
    private static final String proceduresTable = "SYSIBM.SQLPROCEDURES";
    private static final String functionsTable = "QSYS2.SYSFUNCS";
    private static final String parametersTable = "QSYS2.SYSPARMS";
    private static final String tablesTable = "QSYS2.SYSTABLES";
    private static final String viewsTable = "SYSIBM.VIEWS";
    private static final String sequencesTable = "QSYS2.SYSSEQUENCES";

    // Field names
    private static final String dataTypeField = "DATA_TYPE";
    private static final String functionNameField = "ROUTINE_NAME";
    private static final String functionOriginField = "FUNCTION_ORIGIN";
    private static final String functionSchemaField = "FUNCTION_SCHEMA";
    private static final String functionSchemField = "FUNCTION_SCHEM";		// NO 'A' at the end
    private static final String indexSchemaField = "INDEX_SCHEMA";
    private static final String procedureNameField = "PROCEDURE_NAME";
    private static final String procedureSchemField = "PROCEDURE_SCHEM";	// NO 'A' at the end
    private static final String rowTypeField = "ROW_TYPE";
    private static final String schemaNameField = "SCHEMA_NAME";
    private static final String sequenceNameField = "SEQUENCE_NAME";
    private static final String sequenceSchemaField = "SEQUENCE_SCHEMA";
    private static final String specificSchemaField = "SPECIFIC_SCHEMA";
    private static final String specificNameField = "SPECIFIC_NAME";
    private static final String systemTableField = "SYSTEM_TABLE";
    private static final String tableNameField = "TABLE_NAME";
    private static final String tableTypeField = "TABLE_TYPE";
    private static final String tableSchemaField = "TABLE_SCHEMA";
    private static final String viewSchemaField = "VIEW_SCHEMA";
    private static final String routineSchemaField = "ROUTINE_SCHEMA";
    private static final String systemTable = "SYSTEM_TABLE";

    // Value names
    private static final String aliasValue = "A";
    private static final String parameterValue = "P";
    private static final String sequenceValue = "S";
    private static final String tableValue = "T";
    private static final String viewValue = "V";
    private static final String yesValue = "Y";
    private static final String noValue = "N";

    /**
     * Creates a new DB2as400 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport The database-specific support.
     * @param name The name of the schema.
     */
    public DB2as400Schema(JdbcTemplate jdbcTemplate, DB2as400DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        String query = "SELECT COUNT(1) FROM " + schemaTable + " WHERE " + schemaNameField + "=?";
        return jdbcTemplate.queryForInt(query, name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = 0;
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(1) FROM " + tablesTable + " WHERE " + tableSchemaField + " = ? AND " + tableTypeField + " = '" + tableValue + "' AND " + systemTable + " = '" + noValue + "'", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(1) FROM " + tablesTable + " WHERE " + tableSchemaField + " = ? AND " + tableTypeField + " = '" + viewValue + "' AND " + systemTable + " = '" + noValue + "'", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(1) FROM " + proceduresTable + " WHERE " + procedureSchemField + " = ?", name);
        objectCount += jdbcTemplate.queryForInt("SELECT COUNT(1) FROM " + functionsTable + " WHERE " + specificSchemaField + " = ?", name);
        return objectCount == 0;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        String query = "SELECT RTRIM(" + tableNameField + ") FROM "
                + tablesTable + " WHERE " + tableTypeField + "='"
                + tableValue + "' AND "
                + tableSchemaField + " = ? AND "
                + systemTable + " <> '" + yesValue + "'";
        return findTables(query, name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        String query = null;

    	// iSeries V7 query (XML functions are not available on V6)
    	/*
         query = "SELECT f." + specificNameField + ", f." + functionNameField + "," +
         " SUBSTR(xmlserialize(xmlagg(xmltext(CONCAT(', ', " + dataTypeField + "))) AS VARCHAR(1024)), 3) AS PARAMS" +
         " FROM " + functionsTable + " f INNER JOIN " + parametersTable + " p ON f." + specificNameField + " = p." + specificNameField +
         " WHERE f." + functionOriginField + "='Q' " + 
         " AND p." + specificSchemaField + "=? " + 
         " AND p." + rowTypeField + "='" + parameterValue + "'" +
         " GROUP BY f." + specificNameField + ", f."+ functionNameField +
         " ORDER BY f." + specificNameField;
         */
        // iSeries V6/V7 compatible query
        query = "WITH rquery (" + specificSchemaField + "," + specificNameField + ",ordinal, tipo)\n"
                + "         AS\n"
                + "         (\n"
                + "         SELECT f." + specificSchemaField + ", f." + specificNameField + ",par.ordinal_position,par." + dataTypeField + "\n"
                + "         FROM " + functionsTable + " f\n"
                + "         JOIN " + parametersTable + " par ON par." + specificNameField + " = f." + specificNameField + "\n"
                + "         AND f." + specificSchemaField + " = par." + specificSchemaField + "\n"
                + "         WHERE f." + specificSchemaField + " = ?\n"
                + "         AND par.ordinal_position=1\n"
                + "\n"
                + "         UNION ALL\n"
                + "\n"
                + "         SELECT f1." + specificSchemaField + ", f1." + specificNameField + ",f1.ordinal_position,tipo || ',' || f1." + dataTypeField + "\n"
                + "         FROM rquery t0, qsys2.SYSPARMS f1\n"
                + "         WHERE t0." + specificNameField + " = f1." + specificNameField + "\n"
                + "         AND f1." + specificSchemaField + " = ?\n"
                + "         AND t0.ordinal +1 = f1.ordinal_position\n"
                + "         )\n"
                + "         SELECT r." + specificSchemaField + ", r." + specificNameField + " AS Function,\n"
                + "         r.Tipo AS datatype\n"
                + "         FROM rquery r\n"
                + "         WHERE r.ordinal = (SELECT MAX(r1.ordinal) FROM rquery r1 WHERE r1." + specificNameField + "=r." + specificNameField + ")";

        List<Map<String, String>> rows = jdbcTemplate.queryForList(query, name, name);

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
     * Generates DROP statements for this type of table, representing this type
     * of object in this schema.
     *
     * @param schema The schema of the objects.
     * @param tableType The type of table (Can be T, V, S, ...).
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String schema, String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "SELECT RTRIM(" + tableNameField + ") FROM " + tablesTable
                + " WHERE " + tableTypeField + "='" + tableType + "'"
                + " AND " + tableSchemaField + "='" + schema + "'"
                + " AND " + systemTableField + "='" + noValue + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery, schema);
    }

    /**
     * Builds the drop statements for database objects in this schema.
     *
     * @param dropPrefix The drop command for the database object (e.g. 'drop
     * table').
     * @param query The query to get all present database objects
     * @param schema The schema for which to build the statements.
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
        DB2as400Table[] tables = new DB2as400Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2as400Table(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
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
    public Table getTable(String tableName) {
        return new DB2as400Table(jdbcTemplate, dbSupport, this, tableName);
    }
    
    
}
