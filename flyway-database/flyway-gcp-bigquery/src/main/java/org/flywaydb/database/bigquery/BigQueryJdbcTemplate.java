package org.flywaydb.database.bigquery;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.jdbc.JdbcNullTypes;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BigQueryJdbcTemplate extends JdbcTemplate {

    public BigQueryJdbcTemplate(Connection connection) {
        super(connection, DatabaseTypeRegister.getDatabaseTypeForConnection(connection));
    }

    @Override
    protected PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        // Spanner requires specific types for NULL according to the column.
        // This is unlike other databases which have a single "null type".
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                statement.setNull(i + 1, nullType);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) params[i]);
            } else if (params[i] instanceof String) {
                statement.setString(i + 1, params[i].toString());
            } else if (params[i] == JdbcNullTypes.StringNull) {
                // This is the only difference from Spanner - NVARCHAR fails for BigQuery.
                statement.setNull(i + 1, Types.VARCHAR);
            } else if (params[i] == JdbcNullTypes.IntegerNull) {
                statement.setNull(i + 1, Types.INTEGER);
            } else if (params[i] == JdbcNullTypes.BooleanNull) {
                statement.setNull(i + 1, Types.BOOLEAN);
            } else {
                throw new FlywayException("Unhandled object of type '" + params[i].getClass().getName() + "'. ");
            }
        }

        return statement;
    }
}