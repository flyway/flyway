package org.flywaydb.database.cockroachdb;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

@CustomLog
public class CockroachDBConnection extends Connection<CockroachDBDatabase> {
    public CockroachDBConnection(CockroachDBDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        return new CockroachDBSchema(jdbcTemplate, database, name);
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        if (database.supportsSchemas()) {
            String currentSchema = jdbcTemplate.queryForString("SELECT current_schema");
            if (StringUtils.hasText(currentSchema)) {
                return getSchema(currentSchema);
            }

            String searchPath = getCurrentSchemaNameOrSearchPath();
            if (!StringUtils.hasText(searchPath)) {
                throw new FlywayException("Unable to determine current schema as search_path is empty. Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property.");
            }
        }
        return super.doGetCurrentSchema();
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        if (database.supportsSchemas()) {
            String sp = jdbcTemplate.queryForString("SHOW search_path");
            // Up to Cockroach 20, the default response is "public". In 21, that became "$user,public", but this is
            // illegal in the corresponding SET query. Normally this simply results in an exception which we skip over,
            // but in dry runs the produced script will be invalid and error when you run it.
            if (sp.contains("$user")) {
                LOG.debug("Search path contains $user; removing...");
                ArrayList<String> paths = new ArrayList<>(Arrays.asList(sp.split(",")));
                paths.remove("$user");
                sp = String.join(",", paths);
            }
            return sp;
        } else {
            return jdbcTemplate.queryForString("SHOW database");
        }
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            // Avoid unnecessary schema changes as this trips up CockroachDB
            if (schema.getName().equals(originalSchemaNameOrSearchPath) || !schema.exists()) {
                return;
            }
            doChangeCurrentSchemaOrSearchPathTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        if (database.supportsSchemas()) {
            if (!StringUtils.hasLength(schema)) {
                schema = "public";
            }
            jdbcTemplate.execute("SET search_path = " + schema);
        } else {
            if (!StringUtils.hasLength(schema)) {
                schema = "DEFAULT";
            }
            jdbcTemplate.execute("SET database = " + schema);
        }
    }
}