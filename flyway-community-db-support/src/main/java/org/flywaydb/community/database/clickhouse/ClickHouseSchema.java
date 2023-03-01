package org.flywaydb.community.database.clickhouse;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class ClickHouseSchema extends Schema<ClickHouseDatabase, ClickHouseTable> {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param name The name of the schema.
     */
    public ClickHouseSchema(JdbcTemplate jdbcTemplate, ClickHouseDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        int i = jdbcTemplate.queryForInt("SELECT COUNT() FROM system.databases WHERE name = ?", name);
        return i > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int i = jdbcTemplate.queryForInt("SELECT COUNT() FROM system.tables WHERE database = ?", name);
        return i == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.executeStatement("CREATE DATABASE " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        if (jdbcTemplate.getConnection().getSchema().equals(name)) {
            jdbcTemplate.getConnection().setSchema("default");
        }
        jdbcTemplate.executeStatement("DROP DATABASE " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (ClickHouseTable table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected ClickHouseTable[] doAllTables() throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM system.tables WHERE database = ?", name)
                .stream()
                .map(this::getTable)
                .toArray(ClickHouseTable[]::new);
    }

    @Override
    public ClickHouseTable getTable(String tableName) {
        return new ClickHouseTable(jdbcTemplate, database, this, tableName);
    }
}
