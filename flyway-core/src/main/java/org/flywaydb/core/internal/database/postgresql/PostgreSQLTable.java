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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

import java.sql.SQLException;

/**
 * PostgreSQL-specific table.
 */
public class PostgreSQLTable extends Table {
    /**
     * Creates a new PostgreSQL table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    PostgreSQLTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                "   SELECT 1\n" +
                "   FROM   pg_catalog.pg_class c\n" +
                "   JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n" +
                "   WHERE  n.nspname = ?\n" +
                "   AND    c.relname = ?\n" +
                "   AND    c.relkind = 'r'    -- only tables\n" +
                "   );", schema.getName(), name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("SELECT * FROM " + this + " FOR UPDATE");
    }
}
