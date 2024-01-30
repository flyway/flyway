package org.flywaydb.core.internal.database.sqlite;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * SQLite-specific table.
 */
@CustomLog
public class SQLiteTable extends Table<SQLiteDatabase, SQLiteSchema> {
    /**
     * SQLite system tables are undroppable.
     */
    static final String SQLITE_SEQUENCE = "sqlite_sequence";
    private final boolean undroppable;

    /**
     * Creates a new SQLite table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    public SQLiteTable(JdbcTemplate jdbcTemplate, SQLiteDatabase database, SQLiteSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        undroppable = SQLITE_SEQUENCE.equals(name);
    }

    @Override
    protected void doDrop() throws SQLException {
        if (undroppable) {
            LOG.debug("SQLite system table " + this + " cannot be dropped. Ignoring.");
        } else {
            String dropSql = "DROP TABLE " + database.quote(schema.getName(), name);
            if (getSchema().getForeignKeysEnabled()) {
                // #2417: Disable foreign keys before dropping tables to avoid constraint violation errors
                dropSql = "PRAGMA foreign_keys = OFF; " + dropSql + "; PRAGMA foreign_keys = ON";
            }
            jdbcTemplate.execute(dropSql);
        }
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(tbl_name) FROM "
                                                + database.quote(schema.getName()) + ".sqlite_master WHERE type='table' AND tbl_name='" + name + "'") > 0;
    }

    @Override
    protected void doLock() {
        LOG.debug("Unable to lock " + this + " as SQLite does not support locking. No concurrent migration supported.");
    }
}