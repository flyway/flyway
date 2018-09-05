/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL implementation of Schema.
 */
public class RedshiftSchema extends Schema<RedshiftDatabase> {
    /**
     * Creates a new PostgreSQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    RedshiftSchema(JdbcTemplate jdbcTemplate, RedshiftDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return !jdbcTemplate.queryForBoolean("SELECT EXISTS (   SELECT 1\n" +
                "   FROM   pg_catalog.pg_class c\n" +
                "   JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n" +
                "   WHERE  n.nspname = ?)", name);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForRoutines()) {
            jdbcTemplate.execute(statement);
        }
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

        List<String> statements = new ArrayList<>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION " + database.quote(name, row.get("proname")) + "(" + row.get("args") + ") CASCADE");
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
        List<String> statements = new ArrayList<>();
        for (String domainName : viewNames) {
            statements.add("DROP VIEW IF EXISTS " + database.quote(name, domainName) + " CASCADE");
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
                                " AND table_type='BASE TABLE'",
                        name
                );
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new RedshiftTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new RedshiftTable(jdbcTemplate, database, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new RedshiftType(jdbcTemplate, database, this, typeName);
    }
}