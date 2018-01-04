/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL database.
 */
public class PostgreSQLDatabase extends Database {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public PostgreSQLDatabase(FlywayConfiguration configuration, Connection connection



    ) {
        super(configuration, connection, Types.NULL



        );
    }

    @Override
    protected org.flywaydb.core.internal.database.Connection getConnection(Connection connection, int nullType



    ) {
        return new PostgreSQLConnection(configuration, this, connection, nullType



        );
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 9) {
            throw new FlywayDbUpgradeRequiredException("PostgreSQL", version, "9.0");
        }

        if (majorVersion == 9 && minorVersion < 3) {
            throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("PostgreSQL", "PostgreSQL", version);
        }

        if (majorVersion > 10) {
            recommendFlywayUpgrade("PostgreSQL", version);
        }
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource) {
        return new PostgreSQLSqlScript(sqlScriptSource);
    }

    @Override
    public SqlScript createSqlScript(LoadableResource sqlScriptResource, PlaceholderReplacer placeholderReplacer,
                                     String encoding, boolean mixed



    ) {
        return new PostgreSQLSqlScript(sqlScriptResource, placeholderReplacer, encoding, mixed



        );
    }

    @Override
    public String getDbName() {
        return "postgresql";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return mainConnection.getJdbcTemplate().queryForString("SELECT current_user");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return pgQuote(identifier);
    }

    static String pgQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}