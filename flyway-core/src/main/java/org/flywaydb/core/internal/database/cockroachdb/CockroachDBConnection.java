package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.FlywaySqlException;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;

/**
 * CockroachDB connection.
 */
public class CockroachDBConnection extends Connection<CockroachDBDatabase> {
    CockroachDBConnection(FlywayConfiguration configuration, CockroachDBDatabase database, java.sql.Connection connection, int nullType) {
        super(configuration, database, connection, nullType);
    }

    @Override
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        return getSchema(getFirstSchemaFromSearchPath(this.originalSchema));
    }

    private String getFirstSchemaFromSearchPath(String searchPath) {
        String result = searchPath.replace(database.doQuote("$user"), "").trim();
        if (result.startsWith(",")) {
            result = result.substring(1);
        }
        if (result.contains(",")) {
            result = result.substring(0, result.indexOf(","));
        }
        result = result.trim();
        // Unquote if necessary
        if (result.startsWith("\"") && result.endsWith("\"") && !result.endsWith("\\\"") && (result.length() > 1)) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    @Override
    public Schema getSchema(String name) {
        return new CockroachDBSchema(jdbcTemplate, database, name);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SHOW database");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            // Avoid unnecessary schema changes as this trips up CockroachDB
            if (schema.getName().equals(originalSchema) || !schema.exists()) {
                return;
            }
            doChangeCurrentSchemaTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET database = " + schema);
    }
}
