/*-
 * ========================LICENSE_START=================================
 * flyway-doris
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.database.doris;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_AWS_RDS;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_RDS_URL_IDENTIFIER;

@CustomLog
public class DorisDatabase extends Database<DorisConnection> {
    private static final Pattern MYSQL_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+\\w*");

    public DorisDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(rawMainJdbcConnection, databaseType);
    }


    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String baselineMarker = "";
        if (baseline) {
            // Revert to regular insert, which unfortunately is not safe in concurrent scenarios
            // due to MySQL implicit commits after DDL statements.
            baselineMarker = ";\n" + getBaselineStatement(table);
        }

        return "CREATE TABLE " + table + " (\n" +
                "    `installed_rank` INT NOT NULL,\n" +
                "    `version` VARCHAR(50),\n" +
                "    `description` VARCHAR(200) NOT NULL,\n" +
                "    `type` VARCHAR(20) NOT NULL,\n" +
                "    `script` VARCHAR(1000) NOT NULL,\n" +
                "    `checksum` INT,\n" +
                "    `installed_by` VARCHAR(100) NOT NULL,\n" +
                "    `installed_on` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    `execution_time` INT NOT NULL,\n" +
                "    `success` BOOLEAN NOT NULL\n" +
                ")\nUNIQUE KEY(installed_rank)\n" +
                "DISTRIBUTED BY HASH(installed_rank) BUCKETS 1\n" +
                "PROPERTIES (\n" +
                "  \"replication_num\" = \"1\"\n" +
                ")"+baselineMarker +";\n";
    }


    @Override
    protected DorisConnection doGetConnection(Connection connection) {
        return new DorisConnection(this, connection);
    }

    @Override
    protected MigrationVersion determineVersion() {
        // Ignore the version from the JDBC metadata and use the version returned by the database since proxies such as
        // Azure or ProxySQL return incorrect versions
        String selectVersionOutput = BaseDatabaseType.getSelectVersionOutput(rawMainJdbcConnection);
        return extractMySQLVersionFromString(selectVersionOutput);
    }

    static MigrationVersion extractMySQLVersionFromString(String selectVersionOutput) {
        return extractVersionFromString(selectVersionOutput, MYSQL_VERSION_PATTERN);
    }

    /*
     * Given a version string that may contain unwanted text, extract out the version part.
     */
    private static MigrationVersion extractVersionFromString(String versionString, Pattern... patterns) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(versionString);
            if (matcher.find()) {
                return MigrationVersion.fromVersion(matcher.group(1));
            }
        }
        throw new FlywayException("Unable to determine version from '" + versionString + "'");
    }

    @Override
    public void ensureSupported(Configuration configuration) {

        ensureDatabaseIsRecentEnough("5.1");

        //ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("8.0", Tier.PREMIUM, configuration);

        recommendFlywayUpgradeIfNecessary("9.4");

    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT SUBSTRING_INDEX(USER(),'@',1)");
    }

    @Override
    public boolean supportsDdlTransactions() {
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

    @Override
    public String getOpenQuote() {
        return "`";
    }

    @Override
    public String getCloseQuote() {
        return "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }


    @Override
    public String getDatabaseHosting() {
        return super.getDatabaseHosting();
    }
}
