package org.flywaydb.database.informix;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

/**
 * Informix connection.
 */
public class InformixConnection extends Connection<InformixDatabase> {
    InformixConnection(InformixDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return getJdbcConnection().getMetaData().getUserName();
    }

    @Override
    public Schema getSchema(String name) {
        return new InformixSchema(jdbcTemplate, database, name);
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        // Informix doesn't support schemas
    }
}