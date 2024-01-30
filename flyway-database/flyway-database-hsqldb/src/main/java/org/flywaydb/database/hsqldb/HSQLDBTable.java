package org.flywaydb.database.hsqldb;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * HSQLDB-specific table.
 */
@CustomLog
public class HSQLDBTable extends Table<HSQLDBDatabase, HSQLDBSchema> {

    /**
     * Creates a new Hsql table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    HSQLDBTable(JdbcTemplate jdbcTemplate, HSQLDBDatabase database, HSQLDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        if (!database.getVersion().isAtLeast("2")) {
            LOG.debug("Unable to lock " + this + " as Hsql 1.8 does not support locking. No concurrent migration supported.");
            return;
        }
        jdbcTemplate.execute("LOCK TABLE " + this + " WRITE");
    }
}