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
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * H2 database.
 */
public class H2Database extends Database<H2Connection> {
    /**
     * The H2 build id.
     */
    private int buildId;

    /**
     * Whether this version supports DROP SCHEMA ... CASCADE.
     */
    boolean supportsDropSchemaCascade;

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public H2Database(Configuration configuration, Connection connection



    ) {
        super(configuration, connection



        );
    }

    @Override
    protected H2Connection getConnection(Connection connection



    ) {
        return new H2Connection(configuration, this, connection



        );
    }

    @Override
    protected Pair<Integer, Integer> determineMajorAndMinorVersion() {
        Pair<Integer, Integer> majorMinor = super.determineMajorAndMinorVersion();
        try {
            buildId = getMainConnection().getJdbcTemplate().queryForInt(
                    "SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'info.BUILD_ID'");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine H2 build ID", e);
        }
        return majorMinor;
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion + "." + buildId;

        if (majorVersion < 1 || (majorVersion == 1 && minorVersion < 2)) {
            throw new FlywayDbUpgradeRequiredException("H2", version, "1.2.137");
        }
        if ((majorVersion == 1 && (minorVersion > 4 || (minorVersion == 4 && buildId > 197))) || majorVersion > 1) {
            recommendFlywayUpgrade("H2", version);
        }

        supportsDropSchemaCascade =
                MigrationVersion.fromVersion(version).compareTo(MigrationVersion.fromVersion("1.4.197")) >= 0;
    }

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource sqlScriptResource,
                                          PlaceholderReplacer placeholderReplacer, boolean mixed



    ) {
        return new H2SqlScript(sqlScriptResource, placeholderReplacer, mixed



        );
    }

    @Override
    public String getDbName() {
        return "h2";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT USER()");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    protected boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}