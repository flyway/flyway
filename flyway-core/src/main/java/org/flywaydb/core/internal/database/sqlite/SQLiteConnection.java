package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

/**
 * SQLite connection.
 */
public class SQLiteConnection extends Connection<SQLiteDatabase> {
    SQLiteConnection(SQLiteDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        return new SQLiteSchema(jdbcTemplate, database, name);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "main";
    }
}