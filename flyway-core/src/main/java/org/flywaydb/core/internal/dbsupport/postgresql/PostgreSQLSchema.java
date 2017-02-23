/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.postgresql;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL implementation of Schema.
 */
public class PostgreSQLSchema extends Schema<PostgreSQLDbSupport> {
    /**
     * Creates a new PostgreSQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public PostgreSQLSchema(JdbcTemplate jdbcTemplate, PostgreSQLDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        int databaseMajorVersion = jdbcTemplate.getMetaData().getDatabaseMajorVersion();
        int databaseMinorVersion = jdbcTemplate.getMetaData().getDatabaseMinorVersion();

        if ((databaseMajorVersion > 9) || ((databaseMajorVersion == 9) && (databaseMinorVersion >= 3))) {
            // PostgreSQL 9.3 and newer only
            for (String statement : generateDropStatementsForMaterializedViews()) {
                jdbcTemplate.execute(statement);
            }
        }

        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForBaseTypes(true)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForAggregates()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForRoutines()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForEnums()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForDomains()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForBaseTypes(false)) {
            jdbcTemplate.execute(statement);
        }

    }

    /**
     * Generates the statements for dropping the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        List<String> sequenceNames =
                jdbcTemplate.queryForStringList(
                        "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=?", name);

        List<String> statements = new ArrayList<String>();
        for (String sequenceName : sequenceNames) {
            statements.add("DROP SEQUENCE IF EXISTS " + dbSupport.quote(name, sequenceName));
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the types in this schema.
     *
     * @param recreate Flag indicating whether the types should be recreated. Necessary for type-function chicken and egg problem.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForBaseTypes(boolean recreate) throws SQLException {

        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                "select typname, typcategory from pg_catalog.pg_type t "
                + "where (t.typrelid = 0 OR (SELECT c.relkind = 'c' FROM pg_catalog.pg_class c WHERE c.oid = t.typrelid)) and "
                + "NOT EXISTS(SELECT 1 FROM pg_catalog.pg_type el WHERE el.oid = t.typelem AND el.typarray = t.oid) and "
                + "t.typnamespace in (select oid from pg_catalog.pg_namespace where nspname = ?)",
                name);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP TYPE IF EXISTS " + dbSupport.quote(name, row.get("typname")) + " CASCADE");
        }

        if (recreate) {
            for (Map<String, String> row : rows) {
                // Only recreate Pseudo-types (P) and User-defined types (U)
                if (Arrays.asList("P", "U").contains(row.get("typcategory"))) {
                    statements.add("CREATE TYPE " + dbSupport.quote(name, row.get("typname")));
                }
            }
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the aggregates in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForAggregates() throws SQLException {
        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) "
                                + "WHERE pg_proc.proisagg = true AND ns.nspname = ?",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP AGGREGATE IF EXISTS " + dbSupport.quote(name, row.get("proname")) + "(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the routines in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForRoutines() throws SQLException {
        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                // Search for all functions
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) "
                // that don't depend on an extension
                        + "LEFT JOIN pg_depend dep ON dep.objid = pg_proc.oid AND dep.deptype = 'e' "
                        + "WHERE pg_proc.proisagg = false AND ns.nspname = ? AND dep.objid IS NULL",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION IF EXISTS " + dbSupport.quote(name, row.get("proname")) + "(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the enums in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForEnums() throws SQLException {
        List<String> enumNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.typname FROM pg_catalog.pg_type t INNER JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace WHERE n.nspname = ? and t.typtype = 'e'", name);

        List<String> statements = new ArrayList<String>();
        for (String enumName : enumNames) {
            statements.add("DROP TYPE " + dbSupport.quote(name, enumName));
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the domains in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForDomains() throws SQLException {
        List<String> domainNames =
                jdbcTemplate.queryForStringList(
                        "SELECT domain_name FROM information_schema.domains WHERE domain_schema=?", name);

        List<String> statements = new ArrayList<String>();
        for (String domainName : domainNames) {
            statements.add("DROP DOMAIN " + dbSupport.quote(name, domainName));
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the materialized views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForMaterializedViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT relname FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace"
                                + " WHERE c.relkind = 'm' AND n.nspname = ?", name);

        List<String> statements = new ArrayList<String>();
        for (String domainName : viewNames) {
            statements.add("DROP MATERIALIZED VIEW IF EXISTS " + dbSupport.quote(name, domainName) + " CASCADE");
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                // Search for all views
                "SELECT relname FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace" +
                        // that don't depend on an extension
                        " LEFT JOIN pg_depend dep ON dep.objid = c.oid AND dep.deptype = 'e'" +
                        " WHERE c.relkind = 'v' AND  n.nspname = ? AND dep.objid IS NULL",
                name);
        List<String> statements = new ArrayList<String>();
        for (String domainName : viewNames) {
            statements.add("DROP VIEW IF EXISTS " + dbSupport.quote(name, domainName) + " CASCADE");
        }

        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
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
                                " WHERE inhrelid = (quote_ident(t.table_schema)||'.'||quote_ident(t.table_name))::regclass::oid))",
                        name
                );
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new PostgreSQLTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new PostgreSQLTable(jdbcTemplate, dbSupport, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new PostgreSQLType(jdbcTemplate, dbSupport, this, typeName);
    }
}
