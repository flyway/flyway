package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;

/**
 * H2 connection.
 */
public class H2Connection extends Connection<H2Database> {
    H2Connection(FlywayConfiguration configuration, H2Database database, java.sql.Connection connection, int nullType) {
        super(configuration, database, connection, nullType);
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new H2Schema(jdbcTemplate, database, name);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("CALL SCHEMA()");
    }
}
