package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.FlywaySqlException;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * Redshift connection.
 */
public class RedshiftConnection extends Connection<RedshiftDatabase> {
    RedshiftConnection(FlywayConfiguration configuration, RedshiftDatabase database, java.sql.Connection connection, int nullType) {
        super(configuration, database, connection, nullType);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SHOW search_path");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (schema.getName().equals(originalSchema) || originalSchema.startsWith(schema.getName() + ",") || !schema.exists()) {
                return;
            }

            if (StringUtils.hasText(originalSchema) && !"unset".equals(originalSchema)) {
                doChangeCurrentSchemaTo(schema.toString() + "," + originalSchema);
            } else {
                doChangeCurrentSchemaTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if ("unset".equals(schema)) {
            schema = "";
        }
        jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
    }

    @Override
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        return getSchema(getFirstSchemaFromSearchPath(this.originalSchema));
    }

    static String getFirstSchemaFromSearchPath(String searchPath) {
        String result = searchPath
                .replace("\"$user\"", "")
                .replace("$user", "").trim();
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
        return new RedshiftSchema(jdbcTemplate, database, name);
    }
}
