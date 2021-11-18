/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.database.sqlserver.synapse;

import org.flywaydb.database.sqlserver.SQLServerDatabaseType;
import org.flywaydb.database.sqlserver.SQLServerEngineEdition;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

public class SynapseDatabaseType extends SQLServerDatabaseType {
    @Override
    protected boolean supportsJTDS() {
        return false;
    }

    @Override
    public String getName() {
        return "Azure Synapse";
    }

    @Override
    public int getPriority() {
        // Synapse needs to be checked in advance of the plain SQL Server type
        return 1;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        if (databaseProductName.startsWith("Microsoft SQL Server")) {

            try {
                SQLServerEngineEdition engineEdition = SQLServerEngineEdition.fromCode(getJdbcTemplate(connection).queryForInt(
                        "SELECT SERVERPROPERTY('engineedition')"));
                return engineEdition == SQLServerEngineEdition.SQL_DATA_WAREHOUSE;
            }
            catch (SQLException e) {
                throw new FlywaySqlException("Unable to determine database engine edition.'", e);
            }
        }

        return false;
    }

    private JdbcTemplate getJdbcTemplate(Connection connection) {
        return new JdbcTemplate(connection, this);
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new SynapseDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }
}