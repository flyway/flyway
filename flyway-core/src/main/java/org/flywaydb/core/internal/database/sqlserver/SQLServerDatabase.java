/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * SQL Server database.
 */
public class SQLServerDatabase extends Database {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public SQLServerDatabase(FlywayConfiguration configuration, Connection connection



    ) {
        super(configuration, connection, Types.VARCHAR



        );
    }

    @Override
    protected org.flywaydb.core.internal.database.Connection getConnection(Connection connection, int nullType



    ) {
        return new SQLServerConnection(configuration, this, connection, nullType



        );
    }

    @Override
    protected final void ensureSupported() {
        String release = versionToReleaseName(majorVersion, minorVersion);

        if (majorVersion < 10) {
            throw new FlywayDbUpgradeRequiredException("SQL Server", release, "2008");
        }

        if (majorVersion < 12) {
        throw new org.flywaydb.core.internal.database.FlywayEnterpriseUpgradeRequiredException("Microsoft", "SQL Server", release);
        }

        if (majorVersion > 14 || (majorVersion == 14 && minorVersion > 0)) {
            recommendFlywayUpgrade("SQL Server", release);
        }
    }

    private String versionToReleaseName(int major, int minor) {
        if (major < 8) {
            return major + "." + minor;
        }
        if (major == 8) {
            return "2000";
        }
        if (major == 9) {
            return "2005";
        }
        if (major == 10) {
            if (minor == 0) {
                return "2008";
            }
            return "2008 R2";
        }
        if (major == 11) {
            return "2012";
        }
        if (major == 12) {
            return "2014";
        }
        if (major == 13) {
            return "2016";
        }
        if (major == 14) {
            return "2017";
        }
        return major + "." + minor;
    }

    public String getDbName() {
        return "sqlserver";
    }

    @Override
    public Delimiter getDefaultDelimiter() {
        return new Delimiter("GO", true);
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return mainConnection.getJdbcTemplate().queryForString("SELECT SUSER_SNAME()");
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SQLServerSqlStatementBuilder(getDefaultDelimiter());
    }

    /**
     * Escapes this identifier, so it can be safely used in sql queries.
     *
     * @param identifier The identifier to escaped.
     * @return The escaped version.
     */
    private String escapeIdentifier(String identifier) {
        return StringUtils.replaceAll(identifier, "]", "]]");
    }

    @Override
    public String doQuote(String identifier) {
        return "[" + escapeIdentifier(identifier) + "]";
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