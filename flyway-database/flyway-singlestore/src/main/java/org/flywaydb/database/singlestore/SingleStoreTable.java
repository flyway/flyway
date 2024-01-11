package org.flywaydb.database.singlestore;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

@CustomLog
public class SingleStoreTable extends Table<SingleStoreDatabase, SingleStoreSchema> {

    SingleStoreTable(JdbcTemplate jdbcTemplate, SingleStoreDatabase database, SingleStoreSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(schema, null, name);
    }

    @Override
    protected void doLock() throws SQLException {
        if (jdbcTemplate.queryForString("select storage_type from information_schema.tables where table_schema=? and table_name=?", schema.getName(), name).equals("COLUMNSTORE")) {
            LOG.warn("Taking lock on columnstore table is not supported by SingleStoreDB");
        } else {
            jdbcTemplate.execute("SELECT * FROM " + this + " FOR UPDATE");
        }
    }
}