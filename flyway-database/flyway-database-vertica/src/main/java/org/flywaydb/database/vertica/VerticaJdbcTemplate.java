/*-
 * ========================LICENSE_START=================================
 * flyway-database-vertica
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.vertica;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcNullTypes;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.*;

public class VerticaJdbcTemplate extends JdbcTemplate {

    public VerticaJdbcTemplate(Connection connection, DatabaseType databaseType) {
        super(connection, databaseType);
    }

    @Override
    protected PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        // Vertica requires specific types for NULL according to the column.
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
