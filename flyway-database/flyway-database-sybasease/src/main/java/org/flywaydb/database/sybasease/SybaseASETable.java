package org.flywaydb.database.sybasease;

import org.flywaydb.core.internal.database.base.SchemaObject;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * Sybase ASE table.
 */
public class SybaseASETable extends Table<SybaseASEDatabase, SybaseASESchema> {
    /**
     * Creates a new SAP ASE table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    SybaseASETable(JdbcTemplate jdbcTemplate, SybaseASEDatabase database, SybaseASESchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForString("SELECT object_id('" + name + "')") != null;
    }

    @Override
    protected void doLock() throws SQLException {
        // Flyway's locking assumes transactions are being used to release locks on commit at some later point
        // (hence the lack of an 'unlock' method)
        // If multi statement transactions aren't supported, then locking a table makes no sense,
        // since that's the only operation we can do
        if (database.supportsMultiStatementTransactions()) {
            jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE");
        }
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + getName());
    }

    /**
     * Since Sybase ASE does not support schema, dropping out the schema name for toString method
     *
     * @see SchemaObject#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}