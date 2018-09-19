package org.flywaydb.core.internal.database.Cache;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * Cache-specific table.
 */
public class CacheTable extends Table {

    private static final String DURATION = "2";
    /**
     * Creates a new Cache table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    CacheTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("SET OPTION COMPILEMODE = NOCHECK");
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT DECODE((select 1 from %dictionary.compiledclass where SqlSchemaName = ? and SqlTableName = ?), 1, 1, 0)", schema.getName(), name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("LOCK TABLE " + database.quote(schema.getName(), name) + "IN EXCLUSIVE MODE WAIT " + DURATION);
    }
}