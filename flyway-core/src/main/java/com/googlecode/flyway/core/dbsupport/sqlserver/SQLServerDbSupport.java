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
        return (String) jdbcTemplate.queryForObject("SELECT SCHEMA_NAME()", String.class);
    }

    @Override
    public boolean isSchemaEmpty(String schema) {
        int objectCount = jdbcTemplate.queryForInt("Select count(*) FROM " +
                "( " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.TABLES " +
                "Union " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.VIEWS " +
                "Union " +
                "Select CONSTRAINT_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from information_schema.TABLE_CONSTRAINTS " +
                "Union " +
                "Select ROUTINE_NAME as OBJECT_NAME, ROUTINE_SCHEMA as OBJECT_SCHEMA from information_schema.ROUTINES " +
                ") R where OBJECT_SCHEMA = ?", new String[] {schema});
        return objectCount == 0;
    }

    @Override
    public boolean tableExists(final String schema, final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, schema, table, null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public void lockTable(String schema, String table) {
        jdbcTemplate.execute("select * from " + schema + "." + table + " WITH (TABLOCKX)");
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
    public SqlScript createCleanScript(String schema) {
        List<String> statements = cleanForeignKeys(schema);
        statements.addAll(cleanRoutines(schema));
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
     * Cleans the tables in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     */
    private List<String> cleanTables(String schema) {
        @SuppressWarnings({"unchecked"})
        List<String> tableNames = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' and table_schema=?",
                new String[]{schema}, String.class);

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
     */
    private List<String> cleanForeignKeys(String schema) {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> constraintNames =
                jdbcTemplate.queryForList("SELECT table_name, constraint_name FROM information_schema.table_constraints WHERE constraint_type = 'FOREIGN KEY' and table_schema=?",
                        new String[]{schema});

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
     */
    private List<String> cleanRoutines(String schema) {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList("SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema=?",
                        new String[]{schema});

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
     */
    private List<String> cleanViews(String schema) {
        @SuppressWarnings({"unchecked"})
        List<String> viewNames =
                jdbcTemplate.queryForList("SELECT table_name FROM information_schema.views WHERE table_schema=?",
                        new String[]{schema}, String.class);

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW [" + schema + "].[" + viewName + "]");
        }
        return statements;
    }
}
