package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * HSQLDB connection.
 */
public class HSQLDBConnection extends Connection<HSQLDBDatabase> {
    HSQLDBConnection(FlywayConfiguration configuration, HSQLDBDatabase database, java.sql.Connection connection, int nullType) {
        super(configuration, database, connection, nullType);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        ResultSet resultSet = null;
        String schema = null;

        try {
            resultSet = database.getJdbcMetaData().getSchemas();
            while (resultSet.next()) {
                if (resultSet.getBoolean("IS_DEFAULT")) {
                    schema = resultSet.getString("TABLE_SCHEM");
                    break;
                }
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return schema;
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new HSQLDBSchema(jdbcTemplate, database, name);
    }
}
