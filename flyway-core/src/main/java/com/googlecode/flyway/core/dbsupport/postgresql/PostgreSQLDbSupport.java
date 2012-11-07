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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import com.googlecode.flyway.core.migration.sql.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL-specific support.
 */
public class PostgreSQLDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public PostgreSQLDbSupport(Connection connection) {
        super(new PostgreSQLJdbcTemplate(connection));
    }


    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/postgresql/";
    }

    public String getCurrentUserFunction() {
        return "current_user";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT current_schema()");
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                schema);
        return objectCount == 0;
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        return jdbcTemplate.tableExists(null, schema.toLowerCase(), table.toLowerCase(), "TABLE");
    }

    public boolean columnExists(String schema, String table, String column) throws SQLException {
        return jdbcTemplate.columnExists(null, schema, table, column);
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("select * from " + schema + "." + table + " for update");
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new PostgreSQLSqlStatementBuilder();
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        final List<String> allDropStatements = new ArrayList<String>();
        allDropStatements.addAll(generateDropStatementsForTables(schema));
        allDropStatements.addAll(generateDropStatementsForSequences(schema));
        allDropStatements.addAll(generateDropStatementsForBaseTypes(schema, true));
        allDropStatements.addAll(generateDropStatementsForAggregates(schema));
        allDropStatements.addAll(generateDropStatementsForRoutines(schema));
        allDropStatements.addAll(generateDropStatementsForEnums(schema));
        allDropStatements.addAll(generateDropStatementsForDomains(schema));
        allDropStatements.addAll(generateDropStatementsForBaseTypes(schema, false));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String dropStatement : allDropStatements) {
            sqlStatements.add(new SqlStatement(lineNumber, dropStatement));
            lineNumber++;
        }

        return new SqlScript(sqlStatements, this);
    }

    /**
     * Generates the statements for dropping the tables in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForTables(String schema) throws SQLException {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        //Search for all the table names
                        "SELECT t.table_name FROM information_schema.tables t" +
                                //in this schema
                                " WHERE table_schema=?" +
                                //that are real tables (as opposed to views)
                                " AND table_type='BASE TABLE'" +
                                //and are not child tables (= do not inherit from another table).
                                " AND NOT (SELECT EXISTS (SELECT inhrelid FROM pg_catalog.pg_inherits" +
                                " WHERE inhrelid = (t.table_schema||'.'||t.table_name)::regclass::oid))",
                        schema);
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        List<String> statements = new ArrayList<String>();
        for (String tableName : tableNames) {
            statements.add("DROP TABLE \"" + schema + "\".\"" + tableName + "\" CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the sequences in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences(String schema) throws SQLException {
        List<String> sequenceNames =
                jdbcTemplate.queryForStringList(
                        "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=?", schema);

        List<String> statements = new ArrayList<String>();
        for (String sequenceName : sequenceNames) {
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
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForBaseTypes(String schema, boolean recreate) throws SQLException {
        List<String> typeNames =
                jdbcTemplate.queryForStringList(
                        "select typname from pg_catalog.pg_type where typcategory in ('P', 'U') and typnamespace in (select oid from pg_catalog.pg_namespace where nspname = ?)",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (String typeName : typeNames) {
            statements.add("DROP TYPE IF EXISTS \"" + schema + "\".\"" + typeName + "\" CASCADE");
        }

        if (recreate) {
            for (String typeName : typeNames) {
                statements.add("CREATE TYPE \"" + schema + "\".\"" + typeName + "\"");
            }
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the aggregates in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForAggregates(String schema) throws SQLException {
        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) "
                                + "WHERE pg_proc.proisagg = true AND ns.nspname = ?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP AGGREGATE IF EXISTS \"" + schema + "\".\"" + row.get("proname") + "\"(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the routines in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForRoutines(String schema) throws SQLException {
        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) "
                                + "WHERE pg_proc.proisagg = false AND ns.nspname = ?",
                        schema);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION IF EXISTS \"" + schema + "\".\"" + row.get("proname") + "\"(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the enums in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForEnums(String schema) throws SQLException {
        List<String> enumNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.typname FROM pg_catalog.pg_type t INNER JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace WHERE n.nspname = ? and t.typtype = 'e'", schema);

        List<String> statements = new ArrayList<String>();
        for (String enumName : enumNames) {
            statements.add("DROP TYPE \"" + schema + "\".\"" + enumName + "\"");
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the domains in this schema.
     *
     * @param schema The schema for which to generate the statements.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForDomains(String schema) throws SQLException {
        List<String> domainNames =
                jdbcTemplate.queryForStringList(
                        "SELECT domain_name FROM information_schema.domains WHERE domain_schema=?", schema);

        List<String> statements = new ArrayList<String>();
        for (String domainName : domainNames) {
            statements.add("DROP DOMAIN \"" + schema + "\".\"" + domainName + "\"");
        }

        return statements;
    }

    @Override
    public String quote(String identifier) {
        return "\"" + identifier + "\"";
    }
}
