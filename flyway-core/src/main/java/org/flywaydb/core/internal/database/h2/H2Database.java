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
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

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
     */
    public H2Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected H2Connection doGetConnection(Connection connection) {
        return new H2Connection(this, connection);
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
        ensureDatabaseIsRecentEnough("1.2.137");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("1.4", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessary("1.4.199");
        supportsDropSchemaCascade = getVersion().isAtLeast("1.4.199");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE IF NOT EXISTS " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL,\n" +
                "    CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\")\n" +
                ")" +
                // Add special table created marker to compensate for the inability of H2 to lock empty tables
                " AS SELECT -1, NULL, '<< Flyway Schema History table created >>', 'TABLE', '', NULL, '', CURRENT_TIMESTAMP, 0, TRUE;\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "CREATE INDEX \"" + table.getSchema().getName() + "\".\"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
    }

    @Override
    public String getSelectStatement(Table table) {
        return "SELECT " + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_on")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + " FROM " + table
                // Ignore special table created marker
                + " WHERE " + quote("type") + " != 'TABLE'"
                + " AND " + quote("installed_rank") + " > ?"
                + " ORDER BY " + quote("installed_rank");
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