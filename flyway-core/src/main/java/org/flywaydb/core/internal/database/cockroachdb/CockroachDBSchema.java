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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CockroachDB implementation of Schema.
 */
public class CockroachDBSchema extends Schema<CockroachDBDatabase> {
    /**
     * Creates a new CockroachDB schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    CockroachDBSchema(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT EXISTS ( SELECT 1 FROM pg_database WHERE datname=? )", name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        if (database.getMajorVersion() == 1) {
            return !jdbcTemplate.queryForBoolean("SELECT EXISTS (" +
                    "  SELECT 1" +
                    "  FROM information_schema.tables" +
                    "  WHERE table_schema=?" +
                    "  AND table_type='BASE TABLE'" +
                    ")", name);
        }
        return !jdbcTemplate.queryForBoolean("SELECT EXISTS (" +
                "  SELECT 1" +
                "  FROM information_schema.tables " +
                "  WHERE table_catalog=?" +
                "  AND table_schema='public'" +
                "  AND table_type='BASE TABLE'" +
                ")", name);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE DATABASE " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP DATABASE " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }
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
                                " WHERE c.relkind = 'v' AND n.nspname = 'public' AND dep.objid IS NULL");
        List<String> statements = new ArrayList<>();
        for (String domainName : viewNames) {
            statements.add("DROP VIEW IF EXISTS " + database.quote(name, domainName) + " CASCADE");
        }

        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        String query;
        if (database.getMajorVersion() == 1) {
            query =
                    //Search for all the table names
                    "SELECT table_name FROM information_schema.tables" +
                            //in this schema
                            " WHERE table_schema=?" +
                            //that are real tables (as opposed to views)
                            " AND table_type='BASE TABLE'";
        } else {
            query =
                    //Search for all the table names
                    "SELECT table_name FROM information_schema.tables" +
                            //in this database
                            " WHERE table_catalog=?" +
                            " AND table_schema='public'" +
                            //that are real tables (as opposed to views)
                            " AND table_type='BASE TABLE'";
        }

        List<String> tableNames = jdbcTemplate.queryForStringList(query, name);
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new CockroachDBTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new CockroachDBTable(jdbcTemplate, database, this, tableName);
    }
}