package org.flywaydb.database.postgresql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class PostgreSQLConnection extends Connection<PostgreSQLDatabase> {
    private final String originalRole;

    protected PostgreSQLConnection(PostgreSQLDatabase database, java.sql.Connection connection) {
        super(database, connection);

        try {
            originalRole = jdbcTemplate.queryForString("SELECT CURRENT_USER");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine current user", e);
        }
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // Reset the role to its original value in case a migration or callback changed it
        jdbcTemplate.execute("SET ROLE '" + originalRole + "'");
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        String currentSchema = jdbcTemplate.queryForString("SELECT current_schema");
        String searchPath = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(currentSchema) && !StringUtils.hasText(searchPath)) {
            throw new FlywayException("Unable to determine current schema as search_path is empty. " +
                                              "Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property.");
        }

        String schema = StringUtils.hasText(currentSchema) ? currentSchema : searchPath;

        return getSchema(schema);
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

            if (StringUtils.hasText(originalSchemaNameOrSearchPath)) {
                doChangeCurrentSchemaOrSearchPathTo(schema + "," + originalSchemaNameOrSearchPath);
            } else {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
    }

    @Override
    public Schema getSchema(String name) {
        return new PostgreSQLSchema(jdbcTemplate, database, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return new PostgreSQLAdvisoryLockTemplate(database.getConfiguration(), jdbcTemplate, table.toString().hashCode()).execute(callable);
    }
}