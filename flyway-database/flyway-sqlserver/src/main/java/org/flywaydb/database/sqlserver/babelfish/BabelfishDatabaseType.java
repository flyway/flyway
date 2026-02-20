/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.database.sqlserver.babelfish;

import org.flywaydb.database.sqlserver.SQLServerDatabaseType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

public class BabelfishDatabaseType extends SQLServerDatabaseType {
    @Override
    protected boolean supportsJTDS() {
        return false;
    }

    @Override
    public String getName() {
        return "Babelfish";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
            try {
                String babelfish = new JdbcTemplate(connection, this).queryForString(
                        "SELECT CAST(SERVERPROPERTY('Babelfish') AS VARCHAR(10))");
                return "1".equals(babelfish);
            } catch (SQLException e) {
                throw new FlywaySqlException("Unable to check Babelfish property.", e);
            }
        }

        return false;
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new BabelfishDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }
}
