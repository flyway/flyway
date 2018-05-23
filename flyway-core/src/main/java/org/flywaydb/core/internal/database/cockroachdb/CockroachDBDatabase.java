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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * CockroachDB database.
 */
public class CockroachDBDatabase extends Database<CockroachDBConnection> {
    /**
     * Checks whether this connection is pointing at a CockroachDB instance.
     *
     * @param connection The connection.
     * @return {@code true} if it is, {@code false} if not.
     */
    public static boolean isCockroachDB(Connection connection) {
        try {
            return new JdbcTemplate(connection).queryForString("SELECT version()").contains("CockroachDB");
        } catch (Exception e) {
            return false;
        }
    }

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
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;
        if (majorVersion < 1 || (majorVersion == 1 && minorVersion < 1)) {
            throw new FlywayDbUpgradeRequiredException("CockroachDB", version, "1.1");
        }
        if (majorVersion > 2 || (majorVersion == 2 && minorVersion > 0)) {
            recommendFlywayUpgrade("CockroachDB", version);
        }
    }

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource sqlScriptResource,
                                          PlaceholderReplacer placeholderReplacer, boolean mixed



    ) {
        return new CockroachDBSqlScript(configuration, sqlScriptResource, mixed



                , placeholderReplacer);
    }

    @Override
    protected Pair<Integer, Integer> determineMajorAndMinorVersion() {
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
        return Pair.of(majorVersion, minorVersion);
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
    protected boolean supportsChangingCurrentSchema() {
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
}