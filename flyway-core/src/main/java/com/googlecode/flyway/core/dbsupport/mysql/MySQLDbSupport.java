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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mysql-specific support.
 */
public class MySQLDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public MySQLDbSupport(Connection connection) {
        super(new MySQLJdbcTemplate(connection));
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/mysql/";
    }

    public String getCurrentUserFunction() {
        return "SUBSTRING_INDEX(USER(),'@',1)";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.getConnection().getCatalog();
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("Select count(*) FROM " +
                "( " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.TABLES " +
                "Union " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.VIEWS " +
                "Union " +
                "Select CONSTRAINT_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.TABLE_CONSTRAINTS " +
                "Union " +
                "Select ROUTINE_NAME as OBJECT_NAME, ROUTINE_SCHEMA as OBJECT_SCHEMA from information_schema.ROUTINES " +
                ") R " +
                "Where R.OBJECT_SCHEMA=?", schema);
        return objectCount == 0;
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        ResultSet resultSet = jdbcTemplate.getMetaData().getTables(schema, null, table, null);
        return resultSet.next();
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("select * from " + schema + "." + table + " for update");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new MySQLSqlScript(sqlScriptSource, placeholderReplacer);
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        List<String> statements = cleanRoutines(schema);
        statements.addAll(cleanViews(schema));
        statements.addAll(cleanTables(schema));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String statement : statements) {
            sqlStatements.add(new SqlStatement(lineNumber, statement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements);
    }

    /**
     * Generate the statements to clean the tables in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanTables(String schema) throws SQLException {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                        schema);

        List<String> statements = new ArrayList<String>();
        statements.add("SET FOREIGN_KEY_CHECKS = 0");
        for (String tableName : tableNames) {
            statements.add("DROP TABLE `" + schema + "`.`" + tableName + "`");
        }
        statements.add("SET FOREIGN_KEY_CHECKS = 1");
        return statements;
    }

    /**
     * Generate the statements to clean the routines in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanRoutines(String schema) throws SQLException {
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList(
                        "SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema=?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : routineNames) {
            String routineName = row.get("routine_name");
            String routineType = row.get("routine_type");
            statements.add("DROP " + routineType + " `" + schema + "`.`" + routineName + "`");
        }
        return statements;
    }

    /**
     * Generate the statements to clean the views in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanViews(String schema) throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.views WHERE table_schema=?", schema);

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW `" + schema + "`.`" + viewName + "`");
        }
        return statements;
    }
}
