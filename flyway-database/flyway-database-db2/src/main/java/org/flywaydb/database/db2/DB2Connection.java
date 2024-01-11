package org.flywaydb.database.db2;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

/**
 * DB2 connection.
 */
public class DB2Connection extends Connection<DB2Database> {
    DB2Connection(DB2Database database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new DB2Schema(jdbcTemplate, database, name);
    }
}