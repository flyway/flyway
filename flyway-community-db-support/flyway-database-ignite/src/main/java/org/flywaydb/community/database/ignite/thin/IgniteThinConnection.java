package org.flywaydb.community.database.ignite.thin;

import java.sql.SQLException;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

/**
 * Apache Ignite Thin connection.
 */
public class IgniteThinConnection extends Connection<IgniteThinDatabase> {

    IgniteThinConnection(IgniteThinDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        getJdbcConnection().setSchema(schema);
    }

    @Override
    public Schema getSchema(String name) {
        return new IgniteThinSchema(jdbcTemplate, database, name);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return getJdbcConnection().getSchema();
    }
}