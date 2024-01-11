package org.flywaydb.community.database.postgresql.yugabytedb;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.database.postgresql.PostgreSQLSchema;

public class YugabyteDBSchema extends PostgreSQLSchema {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param name The name of the schema.
     */
    public YugabyteDBSchema(JdbcTemplate jdbcTemplate, YugabyteDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    public Table getTable(String tableName) {
        return new YugabyteDBTable(jdbcTemplate, (YugabyteDBDatabase) database, this, tableName);
    }
}