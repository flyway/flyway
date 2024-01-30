package org.flywaydb.database.spanner;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;

import java.util.concurrent.Callable;

public class SpannerConnection extends Connection<SpannerDatabase> {
    protected SpannerConnection(SpannerDatabase database, java.sql.Connection connection) {
        super(database, connection);
        this.jdbcTemplate = new SpannerJdbcTemplate(connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "";
    }

    @Override
    public Schema getSchema(String name) {
        return new SpannerSchema(jdbcTemplate, database, name);
    }
}