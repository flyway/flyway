/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CockroachDB implementation of Schema.
 */
public class CockroachDBSchema extends Schema<CockroachDBDatabase> {
    /**
     * Creates a new PostgreSQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    CockroachDBSchema(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
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
                                " WHERE c.relkind = 'v' AND  n.nspname = ? AND dep.objid IS NULL",
                        name);
        List<String> statements = new ArrayList<String>();
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
                        "SELECT table_name FROM information_schema.tables" +
                                //in this schema
                                " WHERE table_schema=?" +
                                //that are real tables (as opposed to views)
                                " AND table_type='BASE TABLE'",
                        name
                );
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
