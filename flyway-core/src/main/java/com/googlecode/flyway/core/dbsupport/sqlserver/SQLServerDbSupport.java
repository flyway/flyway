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
package com.googlecode.flyway.core.dbsupport.sqlserver;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQLServer-specific support.
 */
public class SQLServerDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public SQLServerDbSupport(Connection connection) {
        super(new SQLServerJdbcTemplate(connection));
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/sqlserver/";
    }

    public String getCurrentUserFunction() {
        return "SUSER_NAME()";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT SCHEMA_NAME()");
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("Select count(*) FROM " +
                "( " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.TABLES " +
                "Union " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.VIEWS " +
                "Union " +
                "Select CONSTRAINT_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                "Union " +
                "Select ROUTINE_NAME as OBJECT_NAME, ROUTINE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.ROUTINES " +
                ") R where OBJECT_SCHEMA = ?", schema);
        return objectCount == 0;
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        return jdbcTemplate.hasTables(null, schema, table);
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("select * from " + schema + "." + table + " WITH (TABLOCKX)");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new SQLServerSqlScript(sqlScriptSource, placeholderReplacer);
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        List<String> statements = cleanForeignKeys(schema);
        statements.addAll(cleanRoutines(schema));
        statements.addAll(cleanViews(schema));
        statements.addAll(cleanTables(schema));
        statements.addAll(cleanTypes(schema));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String statement : statements) {
            sqlStatements.add(new SqlStatement(lineNumber, statement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements);
    }

    /**
     * Cleans the tables in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanTables(String schema) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE' and table_schema=?",
                schema);

        List<String> statements = new ArrayList<String>();
        for (String tableName : tableNames) {
            statements.add("DROP TABLE [" + schema + "].[" + tableName + "]");
        }
        return statements;
    }

    /**
     * Cleans the foreign keys in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanForeignKeys(String schema) throws SQLException {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> constraintNames =
                jdbcTemplate.queryForList(
                        "SELECT table_name, constraint_name FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                " WHERE constraint_type = 'FOREIGN KEY' and table_schema=?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : constraintNames) {
            String tableName = row.get("table_name");
            String constraintName = row.get("constraint_name");
            statements.add("ALTER TABLE [" + schema + "].[" + tableName + "] DROP CONSTRAINT [" + constraintName + "]");
        }
        return statements;
    }

    /**
     * Cleans the routines in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanRoutines(String schema) throws SQLException {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList("SELECT routine_name, routine_type FROM INFORMATION_SCHEMA.ROUTINES" +
                        " WHERE routine_schema=?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : routineNames) {
            String routineName = row.get("routine_name");
            String routineType = row.get("routine_type");
            statements.add("DROP " + routineType + " [" + schema + "].[" + routineName + "]");
        }
        return statements;
    }

    /**
     * Cleans the views in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanViews(String schema) throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList("SELECT table_name FROM INFORMATION_SCHEMA.VIEWS WHERE table_schema=?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW [" + schema + "].[" + viewName + "]");
        }
        return statements;
    }

    /**
     * Cleans the types in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanTypes(String schema) throws SQLException {
        List<String> typeNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id" +
                                " WHERE t.is_user_defined = 1 AND s.name = ?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (String typeName : typeNames) {
            statements.add("DROP TYPE [" + schema + "].[" + typeName + "]");
        }
        return statements;
    }
}
