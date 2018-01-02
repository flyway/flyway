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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * SQLServer-specific table.
 */
public class SQLServerTable extends Table {
    private final String databaseName;

    /**
     * Creates a new SQLServer table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param databaseName The database this table lives in.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    SQLServerTable(JdbcTemplate jdbcTemplate, Database database, String databaseName, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        this.databaseName = databaseName;
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean(
                "SELECT CAST(" +
                        "CASE WHEN EXISTS(" +
                        "  SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?" +
                        ") " +
                        "THEN 1 ELSE 0 " +
                        "END " +
                        "AS BIT)",
                schema.getName(), name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("select * from " + this + " WITH (TABLOCKX)");
    }

    @Override
    public String toString() {
        return database.quote(databaseName, schema.getName(), name);
    }
}
