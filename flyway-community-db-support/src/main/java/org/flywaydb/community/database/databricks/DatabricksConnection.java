package org.flywaydb.community.database.databricks;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

public class DatabricksConnection extends Connection<DatabricksDatabase> {
    protected DatabricksConnection(DatabricksDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return null;
    }

    @Override
    public Schema getSchema(String name) {
        return null;
    }
}
