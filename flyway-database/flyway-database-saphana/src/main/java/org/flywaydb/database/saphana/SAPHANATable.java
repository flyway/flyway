package org.flywaydb.database.saphana;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * SAP HANA-specific table.
 */
public class SAPHANATable extends Table<SAPHANADatabase, SAPHANASchema> {
    /**
     * Creates a new SAP HANA table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    SAPHANATable(JdbcTemplate jdbcTemplate, SAPHANADatabase database, SAPHANASchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.update( "lock table " + this + " in exclusive mode");
    }
}