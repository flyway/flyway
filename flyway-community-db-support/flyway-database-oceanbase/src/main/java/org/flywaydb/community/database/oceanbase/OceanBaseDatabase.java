/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.community.database.oceanbase;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.database.mysql.MySQLConnection;
import org.flywaydb.database.mysql.MySQLDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public class OceanBaseDatabase extends MySQLDatabase {

    public OceanBaseDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected MySQLConnection doGetConnection(Connection connection) {
        return new OceanBaseConnection(this, connection);
    }

    @Override
    protected boolean isCreateTableAsSelectAllowed() {
        return true;
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        ensureDatabaseIsRecentEnough("1.4");
        recommendFlywayUpgradeIfNecessary("4.2");
    }

    @Override
    protected MigrationVersion determineVersion() {
        String versionNumber;
        try {
            versionNumber = OceanBaseJdbcUtils.getVersionNumber(rawMainJdbcConnection);
        } catch (SQLException e) {
            throw new FlywayException("Failed to get version number", e);
        }
        return MigrationVersion.fromVersion(versionNumber);
    }
}
