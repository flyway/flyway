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
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * H2 database.
 */
public class H2Database extends Database<H2Connection> {
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
    public H2Database(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected H2Connection getConnection(Connection connection



    ) {
        return new H2Connection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    protected MigrationVersion determineVersion() {
        try {
            int buildId = getMainConnection().getJdbcTemplate().queryForInt(
                    "SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'info.BUILD_ID'");
            return MigrationVersion.fromVersion(super.determineVersion().getVersion() + "." + buildId);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine H2 build ID", e);
        }
    }

    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("H2", "1.2.137");

        ensureDatabaseIsCompatibleWithFlywayEdition("H2", "H2", "1.4");

        recommendFlywayUpgradeIfNecessary("H2", "1.4.197");
        supportsDropSchemaCascade = getVersion().isAtLeast("1.4.197");
    }

    @Override
    protected SqlStatementBuilderFactory createSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer



    ) {
        return new H2SqlStatementBuilderFactory(placeholderReplacer);
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
    public boolean supportsChangingCurrentSchema() {
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