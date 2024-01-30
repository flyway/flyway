package org.flywaydb.database.mongodb;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class MongoDBTable extends Table<MongoDBDatabase, MongoDBSchema> {
    /**
     * @param jdbcTemplate The JDBC template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public MongoDBTable(JdbcTemplate jdbcTemplate, MongoDBDatabase database, MongoDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("db.getSiblingDB('" + schema.getName() + "')." + name + ".drop()");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("db.getSiblingDB('" + schema.getName() + "').getCollectionNames().filter(function (c) {return c == ('" + name + "');}).length") > 0;
    }

    @Override
    protected void doLock() throws SQLException {

    }
}