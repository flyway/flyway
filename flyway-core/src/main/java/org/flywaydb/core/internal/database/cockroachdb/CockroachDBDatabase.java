/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.AbstractSqlStatementBuilderFactory;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * CockroachDB database.
 */
public class CockroachDBDatabase extends Database<CockroachDBConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public CockroachDBDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected CockroachDBConnection getConnection(Connection connection



    ) {
        return new CockroachDBConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("1.1");
        recommendFlywayUpgradeIfNecessary("2.0");
    }

    @Override
    protected SqlScript getCreateScript(Map<String, String> placeholders) {
        Parser parser = new CockroachDBParser(new FluentConfiguration().placeholders(placeholders));
        return new ParserSqlScript(parser, getRawCreateScript(), false);
    }

    @Override
    protected SqlStatementBuilderFactory createSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer



    ) {
        return new CockroachDBSqlStatementBuilderFactory(placeholderReplacer, configuration);
    }

    @Override
    protected MigrationVersion determineVersion() {
        String version;
        try {
            version = getMainConnection().getJdbcTemplate().queryForString("SELECT value FROM crdb_internal.node_build_info where field='Version'");
            if (version == null) {
                version = getMainConnection().getJdbcTemplate().queryForString("SELECT value FROM crdb_internal.node_build_info where field='Tag'");
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine CockroachDB version", e);
        }
        int firstDot = version.indexOf(".");
        int majorVersion = Integer.parseInt(version.substring(1, firstDot));
        String minorPatch = version.substring(firstDot + 1);
        int minorVersion = Integer.parseInt(minorPatch.substring(0, minorPatch.indexOf(".")));
        return MigrationVersion.fromVersion(majorVersion + "." + minorVersion);
    }

    public String getDbName() {
        return "cockroachdb";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("(SELECT * FROM [SHOW SESSION_USER])");
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }








    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }

    private static class CockroachDBSqlStatementBuilderFactory extends AbstractSqlStatementBuilderFactory {
        private final Configuration configuration;

        CockroachDBSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer, Configuration configuration) {
            super(placeholderReplacer);
            this.configuration = configuration;
        }

        @Override
        public SqlStatementBuilder createSqlStatementBuilder() {
            return null;
        }

        @Override
        public Parser createParser() {
            return new CockroachDBParser(configuration);
        }
    }
}