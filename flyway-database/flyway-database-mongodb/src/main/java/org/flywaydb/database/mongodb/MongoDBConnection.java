package org.flywaydb.database.mongodb;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.logPreviewFeature;

public class MongoDBConnection extends Connection<MongoDBDatabase> {
    protected MongoDBConnection(MongoDBDatabase database, java.sql.Connection connection) {
        super(database, connection);
        this.jdbcTemplate = new MongoDBJdbcTemplate(connection);
        logPreviewFeature("MongoDB support");
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("db.getName()");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {

    }

    @Override
    public Schema getSchema(String name) {
        return new MongoDBSchema(jdbcTemplate, database, name);
    }
}