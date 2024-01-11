package org.flywaydb.database.redshift;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * Redshift connection.
 */
public class RedshiftConnection extends Connection<RedshiftDatabase> {
    RedshiftConnection(RedshiftDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SHOW search_path");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (schema.getName().equals(originalSchemaNameOrSearchPath) || originalSchemaNameOrSearchPath.startsWith(schema.getName() + ",") || !schema.exists()) {
                return;
            }

            if (StringUtils.hasText(originalSchemaNameOrSearchPath) && !"unset".equals(originalSchemaNameOrSearchPath)) {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString() + "," + originalSchemaNameOrSearchPath);
            } else {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        if ("unset".equals(schema)) {
            schema = "";
        }
        jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        String currentSchema = jdbcTemplate.queryForString("SELECT current_schema()");
        String searchPath = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(currentSchema) && !StringUtils.hasText(searchPath)) {
            throw new FlywayException("Unable to determine current schema as search_path is empty. " +
                                              "Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property.");
        }

        String schema = StringUtils.hasText(currentSchema) ? currentSchema : searchPath;

        return getSchema(schema);
    }

    @Override
    public Schema getSchema(String name) {
        return new RedshiftSchema(jdbcTemplate, database, name);
    }
}