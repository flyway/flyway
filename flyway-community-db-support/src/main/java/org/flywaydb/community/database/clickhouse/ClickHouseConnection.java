package org.flywaydb.community.database.clickhouse;

import org.flywaydb.core.internal.database.base.Connection;

import java.sql.SQLException;

public class ClickHouseConnection extends Connection<ClickHouseDatabase> {
    ClickHouseConnection(ClickHouseDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return database.unQuote(getJdbcTemplate().getConnection().getSchema());
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        getJdbcTemplate().getConnection().setSchema(schema);
    }

    @Override
    public ClickHouseSchema getSchema(String name) {
        return new ClickHouseSchema(jdbcTemplate, database, name);
    }
}
