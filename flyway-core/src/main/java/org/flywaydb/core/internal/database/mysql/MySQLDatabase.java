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
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL database.
 */
public class MySQLDatabase extends Database<MySQLConnection> {
    private static final Pattern MARIADB_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+-MariaDB");
    private static final Log LOG = LogFactory.getLog(MySQLDatabase.class);

    /**
     * Whether this is a Percona XtraDB Cluster in strict mode.
     */
    private final boolean pxcStrict;

    /**
     * Whether the event scheduler table is queryable.
     */
    final boolean eventSchedulerQueryable;

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     */
    public MySQLDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );

        JdbcTemplate jdbcTemplate = new JdbcTemplate(rawMainJdbcConnection, databaseType);
        pxcStrict = isRunningInPerconaXtraDBClusterWithStrictMode(jdbcTemplate);
        eventSchedulerQueryable = DatabaseType.MYSQL == databaseType || isEventSchedulerQueryable(jdbcTemplate);
    }

    private static boolean isEventSchedulerQueryable(JdbcTemplate jdbcTemplate) {
        try {
            // Attempt query
            jdbcTemplate.queryForString("SELECT event_name FROM information_schema.events LIMIT 1");
            return true;
        } catch (SQLException e) {
            LOG.debug("Detected unqueryable MariaDB event scheduler, most likely due to it being OFF or DISABLED.");
            return false;
        }
    }

    static boolean isRunningInPerconaXtraDBClusterWithStrictMode(JdbcTemplate jdbcTemplate) {
        try {
            if ("ENFORCING".equals(jdbcTemplate.queryForString(
                    "select VARIABLE_VALUE from performance_schema.global_variables"
                            + " where variable_name = 'pxc_strict_mode'"))) {
                LOG.debug("Detected Percona XtraDB Cluster in strict mode");
                return true;
            }
        } catch (SQLException e) {
            LOG.debug("Unable to detect whether we are running in a Percona XtraDB Cluster. Assuming not to be.");
        }

        return false;
    }

    boolean isMariaDB() {
        return databaseType == DatabaseType.MARIADB;
    }

    boolean isPxcStrict() {
        return pxcStrict;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace =



                        configuration.getTablespace() == null
                        ? ""
                        : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        String baselineMarker = "";
        if (baseline) {
            if (!pxcStrict) {
                baselineMarker = " AS SELECT" +
                        "     1 as \"installed_rank\"," +
                        "     '" + configuration.getBaselineVersion() + "' as \"version\"," +
                        "     '" + configuration.getBaselineDescription() + "' as \"description\"," +
                        "     '" + MigrationType.BASELINE + "' as \"type\"," +
                        "     '" + configuration.getBaselineDescription() + "' as \"script\"," +
                        "     NULL as \"checksum\"," +
                        "     '" + getInstalledBy() + "' as \"installed_by\"," +
                        "     CURRENT_TIMESTAMP as \"installed_on\"," +
                        "     0 as \"execution_time\"," +
                        "     TRUE as \"success\"\n";
            } else {
                // Percona XtraDB Cluster in strict mode doesn't support CREATE TABLE ... AS SELECT ...
                // So revert to regular insert, which unfortunately is not safe in concurrent scenarios
                // due to MySQL implicit commits after DDL statements.
                baselineMarker = ";\n" + getBaselineStatement(table);
            }
        }

        return "CREATE TABLE " + table + " (\n" +
                "    `installed_rank` INT NOT NULL,\n" +
                "    `version` VARCHAR(50),\n" +
                "    `description` VARCHAR(200) NOT NULL,\n" +
                "    `type` VARCHAR(20) NOT NULL,\n" +
                "    `script` VARCHAR(1000) NOT NULL,\n" +
                "    `checksum` INT,\n" +
                "    `installed_by` VARCHAR(100) NOT NULL,\n" +
                "    `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    `execution_time` INT NOT NULL,\n" +
                "    `success` BOOL NOT NULL,\n" +
                "    CONSTRAINT `" + table.getName() + "_pk` PRIMARY KEY (`installed_rank`)\n" +
                ")" + tablespace + " ENGINE=InnoDB" +
                baselineMarker +
                ";\n" +
                "CREATE INDEX `" + table.getName() + "_s_idx` ON " + table + " (`success`);";
    }

    @Override
    protected MySQLConnection doGetConnection(Connection connection) {
        return new MySQLConnection(this, connection);
    }

    @Override
    protected MigrationVersion determineVersion() {
        if (databaseType == DatabaseType.MARIADB) {
            try {
                String productVersion = jdbcMetaData.getDatabaseProductVersion();
                Matcher matcher = MARIADB_VERSION_PATTERN.matcher(productVersion);

                if (!matcher.find()){
                    throw new FlywayException("Unable to determine MariaDB version from '" + productVersion + "'");
                }

                return MigrationVersion.fromVersion(matcher.group(1));

            } catch (SQLException e){
                throw new FlywaySqlException("Unable to determine MariaDB server version", e);
            }
        }

        /*
         * Azure Database for MySQL reports version numbers incorrectly - it claims to be 5.6 (the gateway
         * version) while the db itself is 5.7, visible from SELECT VERSION(). We work around this specific case.
         * This code should be simplified as soon as Azure is fixed.
         * https://docs.microsoft.com/en-us/azure/mysql/concepts-limits#current-known-issues
         */
        MigrationVersion jdbcMetadataVersion = super.determineVersion();
        String selectVersionOutput = DatabaseType.getSelectVersionOutput(rawMainJdbcConnection);
        if (selectVersionOutput.startsWith("5.7") && jdbcMetadataVersion.toString().startsWith("5.6")) {
            LOG.warn("Azure MySQL database - reporting v5.6 in JDBC metadata but database actually v" + selectVersionOutput);
            return MigrationVersion.fromVersion(selectVersionOutput);
        }

        return jdbcMetadataVersion;
    }





















    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("5.1");
        if (databaseType == DatabaseType.MARIADB) {

            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.1", org.flywaydb.core.internal.license.Edition.ENTERPRISE);


            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.2", org.flywaydb.core.internal.license.Edition.PRO);

            recommendFlywayUpgradeIfNecessary("10.4");
        } else {

            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("5.7", org.flywaydb.core.internal.license.Edition.ENTERPRISE);




                if (JdbcUtils.getDriverName(jdbcMetaData).contains("MariaDB")) {
                    LOG.warn("You are connected to a MySQL " + getVersion() + " database using the MariaDB driver." +
                            " This is known to cause issues." +
                            " An upgrade to Oracle's MySQL JDBC driver is highly recommended.");
                }



            recommendFlywayUpgradeIfNecessary("8.0");
        }
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
        return "`" + identifier + "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public boolean useSingleConnection() {
        return !pxcStrict;
    }
}