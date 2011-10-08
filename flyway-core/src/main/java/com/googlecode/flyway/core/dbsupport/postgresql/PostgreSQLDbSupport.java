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

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/postgresql/";
    }

    public String getCurrentUserFunction() {
        return "current_user";
    }

    public String getCurrentSchema() {
        return (String) jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
    }

    public boolean isSchemaEmpty(String schema) {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                new String[]{schema});
        return objectCount == 0;
    }

    public boolean tableExists(final String schema, final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, schema.toLowerCase(),
                        table.toLowerCase(), new String[]{"TABLE"});
                return resultSet.next();
            }
        });
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) {
        jdbcTemplate.execute("select * from " + schema + "." + table + " for update");
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new PostgreSQLSqlScript(sqlScriptSource, placeholderReplacer);
    }

    public SqlScript createCleanScript(String schema) {
        final List<String> allDropStatements = new ArrayList<String>();
        allDropStatements.addAll(generateDropStatementsForTables(schema));
        allDropStatements.addAll(generateDropStatementsForSequences(schema));
        allDropStatements.addAll(generateDropStatementsForBaseTypes(schema, true));
        allDropStatements.addAll(generateDropStatementsForRoutines(schema));
        allDropStatements.addAll(generateDropStatementsForBaseTypes(schema, false));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String dropStatement : allDropStatements) {
            sqlStatements.add(new SqlStatement(lineNumber, dropStatement));
            lineNumber++;
        }

        return new SqlScript(sqlStatements);
    }

    /**
     * Generates the statements for dropping the tables in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForTables(String schema) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> tableNames =
                jdbcTemplate.queryForList(
                        //Search for all the table names
                        "SELECT t.table_name FROM information_schema.tables t" +
                                //in this schema
                                " WHERE table_schema=?" +
                                //that are real tables (as opposed to views)
                                " AND table_type='BASE TABLE'" +
                                //and are not child tables (= do not inherit from another table).
                                " AND NOT (SELECT EXISTS (SELECT inhrelid FROM pg_catalog.pg_inherits" +
                                " WHERE inhrelid = (t.table_schema||'.'||t.table_name)::regclass::oid))",
                        new String[]{schema});
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : tableNames) {
            String tableName = row.get("table_name");
            statements.add("DROP TABLE \"" + schema + "\".\"" + tableName + "\" CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the sequences in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForSequences(String schema) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> sequenceNames =
                jdbcTemplate.queryForList(
                        "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=?", new String[]{schema});

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : sequenceNames) {
            String sequenceName = row.get("sequence_name");
            statements.add("DROP SEQUENCE IF EXISTS \"" + schema + "\".\"" + sequenceName + "\"");
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the types in this schema.
     *
     * @param schema   The schema for which to generate the statements.
     * @param recreate Flag indicating whether the types should be recreated. Necessary for type-function chicken and egg problem.
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForBaseTypes(String schema, boolean recreate) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> typeNames =
                jdbcTemplate.queryForList(
                        "select typname from pg_catalog.pg_type where typcategory in ('P', 'U') and typnamespace in (select oid from pg_catalog.pg_namespace where nspname = ?)",
                        new String[]{schema});

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : typeNames) {
            String typeName = row.get("typname");
            statements.add("DROP TYPE IF EXISTS \"" + schema + "\".\"" + typeName + "\" CASCADE");
        }

        if (recreate) {
            for (Map<String, String> row : typeNames) {
                String typeName = row.get("typname");
                statements.add("CREATE TYPE \"" + schema + "\".\"" + typeName + "\"");
            }
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the routines in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     */
    private List<String> generateDropStatementsForRoutines(String schema) {
        @SuppressWarnings({"unchecked"}) List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) WHERE ns.nspname = ?", new String[]{schema});

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION IF EXISTS \"" + schema + "\".\"" + row.get("proname") + "\"(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }
}
