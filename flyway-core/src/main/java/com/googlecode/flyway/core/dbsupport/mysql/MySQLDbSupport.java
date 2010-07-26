/**
 * Copyright (C) 2009-2010 the original author or authors.
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
 * Mysql-specific support.
 */
public class MySQLDbSupport implements DbSupport {
    @Override
    public String getCreateMetaDataTableScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/mysql/createMetaDataTable.sql";
    }

    @Override
    public String getCurrentSchema(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getCatalog();
            }
        });
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "MySQL".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(final JdbcTemplate jdbcTemplate, final String schemaMetaDataTable) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(getCurrentSchema(jdbcTemplate), null,
                        schemaMetaDataTable, null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsLocking() {
        return true;
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new MySQLSqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript(JdbcTemplate jdbcTemplate) {
        int lineNumber = 0;
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();

        lineNumber = cleanRoutines(jdbcTemplate, lineNumber, sqlStatements);
        lineNumber = cleanViews(jdbcTemplate, lineNumber, sqlStatements);
        cleanTables(jdbcTemplate, lineNumber, sqlStatements);

        return new SqlScript(sqlStatements);
    }

    /**
     * Cleans the tables in this schema.
     *
     * @param jdbcTemplate The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     */
    private void cleanTables(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> tableNames =
                jdbcTemplate.queryForList(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                        new Object[]{getCurrentSchema(jdbcTemplate)});
        lineNumber++;
        sqlStatements.add(new SqlStatement(lineNumber, "SET FOREIGN_KEY_CHECKS = 0"));
        for (Map<String, String> row : tableNames) {
            lineNumber++;
            String tableName = row.get("table_name");
            sqlStatements.add(new SqlStatement(lineNumber, "DROP TABLE " + tableName));
        }
        lineNumber++;
        sqlStatements.add(new SqlStatement(lineNumber, "SET FOREIGN_KEY_CHECKS = 1"));
    }

    /**
     * Cleans the routines in this schema.
     *
     * @param jdbcTemplate The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     * @return The final line number.
     */
    private int cleanRoutines(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList(
                        "SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema=?",
                        new Object[]{getCurrentSchema(jdbcTemplate)});
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
     * @param jdbcTemplate The jdbcTemplate pointing to the schema to clean.
     * @param lineNumber The initial line number of the clean script.
     * @param sqlStatements The statement list to add to.
     * @return The final line number.
     */
    private int cleanViews(JdbcTemplate jdbcTemplate, int lineNumber, List<SqlStatement> sqlStatements) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> viewNames =
                jdbcTemplate.queryForList(
                        "SELECT table_name FROM information_schema.views WHERE table_schema=?",
                        new Object[]{getCurrentSchema(jdbcTemplate)});
        for (Map<String, String> row : viewNames) {
            lineNumber++;
            String viewName = row.get("table_name");
            sqlStatements.add(new SqlStatement(lineNumber, "DROP VIEW " + viewName));
        }
        return lineNumber;
    }
}
