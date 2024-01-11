package org.flywaydb.database.firebird;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class FirebirdConnection extends Connection<FirebirdDatabase> {

    private static final String DUMMY_SCHEMA_NAME = "default";

    FirebirdConnection(FirebirdDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return DUMMY_SCHEMA_NAME;
    }

    @Override
    public Schema getSchema(String name) {
        // database == schema, always return the same dummy schema
        return new FirebirdSchema(jdbcTemplate, database, DUMMY_SCHEMA_NAME);
    }
}