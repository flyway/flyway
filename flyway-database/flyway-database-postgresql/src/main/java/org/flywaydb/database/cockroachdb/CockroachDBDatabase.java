/*-
 * ========================LICENSE_START=================================
 * flyway-database-postgresql
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
package org.flywaydb.database.cockroachdb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class CockroachDBDatabase extends Database<CockroachDBConnection> {

    private final MigrationVersion determinedVersion;

    public CockroachDBDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
        this.determinedVersion = rawDetermineVersion(jdbcConnectionFactory.getDatabaseType());
    }

    @Override
    protected CockroachDBConnection doGetConnection(final Connection connection) {
        return new CockroachDBConnection(this, connection);
    }

    @Override
    public void ensureSupported(final Configuration configuration) {
        ensureDatabaseIsRecentEnough("1.1");
        recommendFlywayUpgradeIfNecessary("26.1");
    }

    @Override
    public String getRawCreateScript(final Table table, final boolean baseline) {
        return "CREATE TABLE IF NOT EXISTS "
            + table
            + " (\n"
            + "    \"installed_rank\" INT NOT NULL PRIMARY KEY,\n"
            + "    \"version\" VARCHAR(50),\n"
            + "    \"description\" VARCHAR(200) NOT NULL,\n"
            + "    \"type\" VARCHAR(20) NOT NULL,\n"
            + "    \"script\" VARCHAR(1000) NOT NULL,\n"
            + "    \"checksum\" INTEGER,\n"
            + "    \"installed_by\" VARCHAR(100) NOT NULL,\n"
            + "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n"
            + "    \"execution_time\" INTEGER NOT NULL,\n"
            + "    \"success\" BOOLEAN NOT NULL\n"
            + ");\n"
            + (baseline ? getBaselineStatement(table) + ";\n" : "")
            + "CREATE INDEX IF NOT EXISTS \""
            + table.getName()
            + "_s_idx\" ON "
            + table
            + " (\"success\");";
    }

    private MigrationVersion rawDetermineVersion(final DatabaseType databaseType) {
        final String version;
        try {
            // Use rawMainJdbcConnection to avoid infinite recursion.
            final JdbcTemplate template = new JdbcTemplate(rawMainJdbcConnection, databaseType);
            final String versionString = template.queryForString("SELECT version()");
            final Pattern versionPattern = Pattern.compile("v\\S+");
            final Matcher matcher = versionPattern.matcher(versionString);
            if (!matcher.find()) {
                throw new FlywayException("Unable to determine CockroachDB version");
            }
            version = matcher.group();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine CockroachDB version", e);
        }
        final int firstDot = version.indexOf(".");
        final int majorVersion = Integer.parseInt(version.substring(1, firstDot));
        final String minorPatch = version.substring(firstDot + 1);
        final int minorVersion = Integer.parseInt(minorPatch.substring(0, minorPatch.indexOf(".")));
        return MigrationVersion.fromVersion(majorVersion + "." + minorVersion);
    }

    @Override
    protected MigrationVersion determineVersion() {
        return determinedVersion;
    }

    boolean supportsSchemas() {
        return getVersion().isAtLeast("20.2");
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT * FROM [SHOW SESSION_USER]");
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(final String identifier) {
        return getOpenQuote()
            + StringUtils.replaceAll(identifier, getCloseQuote(), getEscapedQuote())
            + getCloseQuote();
    }

    @Override
    public String getEscapedQuote() {
        return "\"\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }
}
