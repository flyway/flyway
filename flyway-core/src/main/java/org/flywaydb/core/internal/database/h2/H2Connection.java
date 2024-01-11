package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

public class H2Connection extends Connection<H2Database> {
    private final boolean requiresV2Metadata;

    H2Connection(H2Database database, java.sql.Connection connection, boolean requiresV2Metadata) {
        super(database, connection);
        this.requiresV2Metadata = requiresV2Metadata;
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new H2Schema(jdbcTemplate, database, name, requiresV2Metadata);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("CALL SCHEMA()");
    }
}