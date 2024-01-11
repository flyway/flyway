package org.flywaydb.database.derby;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

/**
 * Derby connection.
 */
public class DerbyConnection extends Connection<DerbyDatabase> {
    DerbyConnection(DerbyDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new DerbySchema(jdbcTemplate, database, name);
    }
}