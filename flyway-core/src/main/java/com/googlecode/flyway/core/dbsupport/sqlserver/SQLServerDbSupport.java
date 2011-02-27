/**
 * Copyright (C) 2010-2011 the original author or authors.
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MSsql-specific support.
 */
public class SQLServerDbSupport implements DbSupport {
    /**
     * The jdbcTemplate to use.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance.
     *
     * @param jdbcTemplate The jdbcTemplate to use.
     */
    public SQLServerDbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/sqlserver/";
    }

    @Override
    public String getCurrentUserFunction() {
        return "SUSER_NAME()";
    }

    @Override
    public String getCurrentSchema() {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getCatalog();
            }
        });
    }

    @Override
    public boolean isSchemaEmpty() {
        int objectCount = jdbcTemplate.queryForInt("Select count(*) FROM " +
                "( " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.TABLES " +
                "Union " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.VIEWS " +
                "Union " +
                "Select CONSTRAINT_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.TABLE_CONSTRAINTS " +
                "Union " +
                "Select ROUTINE_NAME as OBJECT_NAME, ROUTINE_SCHEMA as OBJECT_SCHEMA from information_schema.ROUTINES " +
                ") R ");
        return objectCount == 0;
    }

    @Override
    public boolean tableExists(final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(getCurrentSchema(), null,
                        table, null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean columnExists(final String table, final String column) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getColumns(getCurrentSchema(), null,
                        table, column);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public boolean supportsLocking() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new SQLServerSqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript() {
        int lineNumber = 0;
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();

        lineNumber = cleanForeignKeys(jdbcTemplate, lineNumber, sqlStatements);
        lineNumber = cleanRoutines(jdbcTemplate, lineNumber, sqlStatements);
        lineNumber = cleanViews(jdbcTemplate, lineNumber, sqlStatements);
        cleanTables(jdbcTemplate, lineNumber, sqlStatements);

        return new SqlScript(sqlStatements);
    }

    /**
     * Cleans the tables in this schema.
     *
     * @param jdbcTemplate  The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber    The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     */
    private void cleanTables(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> tableNames =
                jdbcTemplate.queryForList(
                        "SELECT table_schema, table_name FROM information_schema.tables WHERE table_type='BASE TABLE'");
        lineNumber++;

        for (Map<String, String> row : tableNames) {
            lineNumber++;
            String tableSchema = row.get("table_schema");
            String tableName = row.get("table_name");

            sqlStatements.add(new SqlStatement(lineNumber, "DROP TABLE [" + tableSchema + "].[" + tableName + "]"));
        }
    }

    /**
     * Cleans the foreign keys in this schema.
     *
     * @param jdbcTemplate  The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber    The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     *
     * @return The final line number.
     */
    private int cleanForeignKeys(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> constraintNames =
                jdbcTemplate.queryForList("SELECT table_schema, table_name, constraint_name FROM information_schema.table_constraints WHERE constraint_type = 'FOREIGN KEY'");
        for (Map<String, String> row : constraintNames) {
            lineNumber++;
            String tableSchema = row.get("table_schema");
            String tableName = row.get("table_name");
            String constraintName = row.get("constraint_name");
            sqlStatements.add(new SqlStatement(lineNumber, "ALTER TABLE [" + tableSchema + "].[" + tableName + "] DROP CONSTRAINT [" + constraintName + "]"));
        }
        return lineNumber;
    }

    /**
     * Cleans the routines in this schema.
     *
     * @param jdbcTemplate  The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber    The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     *
     * @return The final line number.
     */
    private int cleanRoutines(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList("SELECT routine_name, routine_type FROM information_schema.routines");
        for (Map<String, String> row : routineNames) {
            lineNumber++;
            String routineName = row.get("routine_name");
            String routineType = row.get("routine_type");
            sqlStatements.add(new SqlStatement(lineNumber, "DROP " + routineType + " " + routineName));
        }
        return lineNumber;
    }

    /**
     * Cleans the views in this schema.
     *
     * @param jdbcTemplate  The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber    The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     *
     * @return The final line number.
     */
    private int cleanViews(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> viewNames =
                jdbcTemplate.queryForList("SELECT table_name FROM information_schema.views");
        for (Map<String, String> row : viewNames) {
            lineNumber++;
            String viewName = row.get("table_name");
            sqlStatements.add(new SqlStatement(lineNumber, "DROP VIEW " + viewName));
        }
        return lineNumber;
    }
}
