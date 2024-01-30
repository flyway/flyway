package org.flywaydb.database.snowflake;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SnowflakeTable extends Table<SnowflakeDatabase, SnowflakeSchema> {
    SnowflakeTable(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, SnowflakeSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName()) + "." + database.quote(name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (!schema.exists()) {
            return false;
        }

        String sql = "SHOW TABLES LIKE '" + name + "' IN SCHEMA " + database.quote(schema.getName());
        List<Boolean> results = jdbcTemplate.query(sql, new RowMapper<Boolean>() {
            @Override
            public Boolean mapRow(ResultSet rs) throws SQLException {
                return true;
            }
        });
        return !results.isEmpty();
    }

    @Override
    protected void doLock() throws SQLException {
        // no-op
    }
}