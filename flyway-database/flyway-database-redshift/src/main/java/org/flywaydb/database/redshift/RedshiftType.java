package org.flywaydb.database.redshift;

import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * PostgreSQL-specific type.
 */
public class RedshiftType extends Type<RedshiftDatabase, RedshiftSchema> {
    /**
     * Creates a new PostgreSQL type.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this type lives in.
     * @param name The name of the type.
     */
    public RedshiftType(JdbcTemplate jdbcTemplate, RedshiftDatabase database, RedshiftSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}