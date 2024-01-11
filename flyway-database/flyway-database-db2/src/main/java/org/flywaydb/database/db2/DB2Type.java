package org.flywaydb.database.db2;

import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * Db2-specific type.
 */
public class DB2Type extends Type<DB2Database, DB2Schema> {
    /**
     * Creates a new Db2 type.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this type lives in.
     * @param name The name of the type.
     */
    DB2Type(JdbcTemplate jdbcTemplate, DB2Database database, DB2Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}