package org.flywaydb.database.db2;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * DB2-specific function.
 */
public class DB2Function extends Function {
    /**
     * Creates a new Db2 function.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this function lives in.
     * @param name The name of the function.
     * @param args The arguments of the function.
     */
    DB2Function(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name, String... args) {
        super(jdbcTemplate, database, schema, name, args);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SPECIFIC FUNCTION " + database.quote(schema.getName(), name));
    }
}