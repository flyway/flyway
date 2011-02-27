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
package com.googlecode.flyway.core.dbsupport.postgresql;

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
 * PostgreSQL-specific support.
 */
public class PostgreSQLDbSupport implements DbSupport {

    private final static String DEFAULT_SCHEMA_PATTERN = "public";
    private final static String[] TABLE_EXISTS_TABLE_TYPES = new String[]{"TABLE"};

    /**
     * The jdbcTemplate to use.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance.
     *
     * @param jdbcTemplate The jdbcTemplate to use.
     */
    public PostgreSQLDbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/postgresql/";
    }

    @Override
    public String getCurrentUserFunction() {
        return "current_user";
    }

    @Override
    public String getCurrentSchema() {
        return (String) jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
    }

    @Override
    public boolean isSchemaEmpty() {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema=current_schema() AND table_type='BASE TABLE'");
        return objectCount == 0;
    }

    @Override
    public boolean tableExists(final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(getCurrentSchema(), DEFAULT_SCHEMA_PATTERN,
                        table.toLowerCase(), TABLE_EXISTS_TABLE_TYPES);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean columnExists(final String table, final String column) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getColumns(getCurrentSchema(), DEFAULT_SCHEMA_PATTERN,
                        table.toLowerCase(), column.toLowerCase());
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public void lockTable(String table) {
        jdbcTemplate.execute("select * from " + table + " for update");
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new PostgreSQLSqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript() {
        final List<String> allDropStatements = new ArrayList<String>();
        allDropStatements.addAll(generateDropStatementsForTables());
        allDropStatements.addAll(generateDropStatementsForSequences());
        allDropStatements.addAll(generateDropStatementsForRoutines());

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int count = 0;
        for (String dropStatement : allDropStatements) {
            count++;
            sqlStatements.add(new SqlStatement(count, dropStatement));
        }

        return new SqlScript(sqlStatements);
    }

    /**
     * Generates the statements for dropping the tables in this schema.
     *
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForTables() {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> tableNames =
                jdbcTemplate.queryForList(
                        "SELECT table_name FROM information_schema.tables WHERE " +
                                "table_schema=current_schema() AND table_type='BASE TABLE'");

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : tableNames) {
            String tableName = row.get("table_name");
            statements.add("DROP TABLE IF EXISTS \"" + tableName + "\" CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the sequences in this schema.
     *
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForSequences() {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> sequenceNames =
                jdbcTemplate.queryForList(
                        "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=current_schema()");

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : sequenceNames) {
            String sequenceName = row.get("sequence_name");
            statements.add("DROP SEQUENCE IF EXISTS \"" + sequenceName + "\"");
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the routines in this schema.
     *
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForRoutines() {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) WHERE ns.nspname = current_schema()");

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION \"" + row.get("proname") + "\"(" + row.get("args") + ")");
        }
        return statements;
    }
}
