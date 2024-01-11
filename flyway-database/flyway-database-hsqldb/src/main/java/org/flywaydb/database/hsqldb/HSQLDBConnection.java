package org.flywaydb.database.hsqldb;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * HSQLDB connection.
 */
public class HSQLDBConnection extends Connection<HSQLDBDatabase> {
    HSQLDBConnection(HSQLDBDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
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
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new HSQLDBSchema(jdbcTemplate, database, name);
    }
}