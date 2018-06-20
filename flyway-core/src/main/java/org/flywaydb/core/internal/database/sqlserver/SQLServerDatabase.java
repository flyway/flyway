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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.StringResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * SQL Server database.
 */
public class SQLServerDatabase extends Database<SQLServerConnection> {
    private final boolean azure;

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public SQLServerDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
        try {
            azure = "SQL Azure".equals(getMainConnection().getJdbcTemplate().queryForString(
                    "SELECT CAST(SERVERPROPERTY('edition') AS VARCHAR)"));
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine database edition", e);
        }
    }

    @Override
    protected SQLServerConnection getConnection(Connection connection



    ) {
        return new SQLServerConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    protected final void ensureSupported() {
        String release = versionToReleaseName(majorVersion, minorVersion);

        if (majorVersion < 10) {
            throw new FlywayDbUpgradeRequiredException("SQL Server", release, "2008");
        }

        if (majorVersion < 12) {
        throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("Microsoft", "SQL Server", release);
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

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource sqlScriptResource,
                                          PlaceholderReplacer placeholderReplacer, boolean mixed



    ) {
        return new SQLServerSqlScript(configuration, sqlScriptResource, mixed



                , placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "sqlserver";
    }

    @Override
    public Delimiter getDefaultDelimiter() {
        return new Delimiter("GO", true);
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT SUSER_SNAME()");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    protected boolean supportsChangingCurrentSchema() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
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

    @Override
    protected LoadableResource getRawCreateScript() {
        return new StringResource("CREATE TABLE ${table_quoted} (\n" +
                "    [installed_rank] INT NOT NULL,\n" +
                "    [" + "version] NVARCHAR(50),\n" +
                "    [description] NVARCHAR(200),\n" +
                "    [type] NVARCHAR(20) NOT NULL,\n" +
                "    [script] NVARCHAR(1000) NOT NULL,\n" +
                "    [checksum] INT,\n" +
                "    [installed_by] NVARCHAR(100) NOT NULL,\n" +
                "    [installed_on] DATETIME NOT NULL DEFAULT GETDATE(),\n" +
                "    [execution_time] INT NOT NULL,\n" +
                "    [success] BIT NOT NULL\n" +
                ");\n" +
                "ALTER TABLE ${table_quoted} ADD CONSTRAINT [${table}_pk] PRIMARY KEY ([installed_rank]);\n" +
                "\n" +
                "CREATE INDEX [${table}_s_idx] ON ${table_quoted} ([success]);\n" +
                "GO\n");
    }

    /**
     * @return Whether this is a SQL Azure database.
     */
    boolean isAzure() {
        return azure;
    }
}