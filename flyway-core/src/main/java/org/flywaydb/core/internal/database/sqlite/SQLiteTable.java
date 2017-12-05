/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.SQLException;

/**
 * SQLite-specific table.
 */
public class SQLiteTable extends Table {
    private static final Log LOG = LogFactory.getLog(SQLiteTable.class);

    /** SQLite system tables are undroppable. */
    static final String SQLITE_SEQUENCE = "sqlite_sequence";
    private final boolean undroppable;

    /**
     * Creates a new SQLite table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public SQLiteTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        undroppable = SQLITE_SEQUENCE.equals(name);
    }

    @Override
    protected void doDrop() throws SQLException {
        if (undroppable) {
            LOG.debug("SQLite system table " + this + " cannot be dropped. Ignoring.");
        } else {
            jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
        }
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(tbl_name) FROM "
                + database.quote(schema.getName()) + ".sqlite_master WHERE type='table' AND tbl_name='" + name + "'") > 0;
    }

    @Override
    protected void doLock() throws SQLException {
        LOG.debug("Unable to lock " + this + " as SQLite does not support locking. No concurrent migration supported.");
    }
}
