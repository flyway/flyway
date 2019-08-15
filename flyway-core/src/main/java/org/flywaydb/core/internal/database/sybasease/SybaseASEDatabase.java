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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Sybase ASE database.
 */
public class SybaseASEDatabase extends Database<SybaseASEConnection> {
    /**
     * Creates a new Sybase ASE database.
     *
     * @param configuration The Flyway configuration.
     */
    public SybaseASEDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected SybaseASEConnection doGetConnection(Connection connection) {
        return new SybaseASEConnection(this, connection);
    }









    @Override
    public void ensureSupported() {
        ensureDatabaseIsRecentEnough("15.7");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("16.0", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessary("16.2");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table.getName() + " (\n" +
                "    installed_rank INT NOT NULL,\n" +
                "    version VARCHAR(50) NULL,\n" +
                "    description VARCHAR(200) NOT NULL,\n" +
                "    type VARCHAR(20) NOT NULL,\n" +
                "    script VARCHAR(1000) NOT NULL,\n" +
                "    checksum INT NULL,\n" +
                "    installed_by VARCHAR(100) NOT NULL,\n" +
                "    installed_on datetime DEFAULT getDate() NOT NULL,\n" +
                "    execution_time INT NOT NULL,\n" +
                "    success decimal NOT NULL,\n" +
                "    PRIMARY KEY (installed_rank)\n" +
                ")\n" +
                "lock datarows on 'default'\n" +
                (baseline ? getBaselineStatement(table) + "\n" : "") +
                "go\n" +
                "CREATE INDEX " + table.getName() + "_s_idx ON " + table.getName() + " (success)\n" +
                "go\n";
    }

    @Override
    public Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT user_name()");
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
    protected String doQuote(String identifier) {
        //Sybase doesn't quote identifiers, skip quoting.
        return identifier;
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

}