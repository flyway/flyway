/*
 * Copyright 2010-2020 Redgate Software Ltd
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
    // See https://mariadb.com/kb/en/version/
    private static final Pattern MARIADB_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+(-\\d+)*-MariaDB(-\\w+)*");
    private static final Pattern MARIADB_WITH_MAXSCALE_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+(-\\d+)* (\\d+\\.\\d+)\\.\\d+(-\\d+)*-maxscale(-\\w+)*");
    private static final Pattern MYSQL_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+\\w*");
    private static final Log LOG = LogFactory.getLog(MySQLDatabase.class);

    /**
     * Whether this is a Percona XtraDB Cluster in strict mode.
     */
    private final boolean pxcStrict;

    /**
     * Whether this database is enforcing GTID consistency.
     */
    private final boolean gtidConsistencyEnforced;

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
        pxcStrict = isMySQL() && isRunningInPerconaXtraDBClusterWithStrictMode(jdbcTemplate);
        gtidConsistencyEnforced = isMySQL() && isRunningInGTIDConsistencyMode(jdbcTemplate);
        eventSchedulerQueryable = isMySQL() || isEventSchedulerQueryable(jdbcTemplate);
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
            String pcx_strict_mode = jdbcTemplate.queryForString(
                    "select VARIABLE_VALUE from performance_schema.global_variables"
                            + " where variable_name = 'pxc_strict_mode'");
            if ("ENFORCING".equals(pcx_strict_mode) || "MASTER".equals(pcx_strict_mode)) {
                LOG.debug("Detected Percona XtraDB Cluster in strict mode");
                return true;
            }
        } catch (SQLException e) {
            LOG.debug("Unable to detect whether we are running in a Percona XtraDB Cluster. Assuming not to be.");
        }

        return false;
    }

   static boolean isRunningInGTIDConsistencyMode(JdbcTemplate jdbcTemplate) {
        try {
            String gtidConsistency = jdbcTemplate.queryForString("SELECT @@GLOBAL.ENFORCE_GTID_CONSISTENCY");
            if ("ON".equals(gtidConsistency)) {
                LOG.debug("Detected GTID consistency being enforced");
                return true;
            }
        } catch (SQLException e) {
            LOG.debug("Unable to detect whether database enforces GTID consistency. Assuming not.");
        }

        return false;
    }

    boolean isMySQL() {
        return databaseType == DatabaseType.MYSQL;
    }

    boolean isMariaDB() {
        return databaseType == DatabaseType.MARIADB;
    }

    boolean isPxcStrict() {
        return pxcStrict;
    }

    /*
     * CREATE TABLE ... AS SELECT ... cannot be used in two scenarios:
     * - Percona XtraDB Cluster in strict mode doesn't support it
     * - When GTID consistency is being enforced. Note that if GTID_MODE is ON, then ENFORCE_GTID_CONSISTENCY is
     * necessarily ON as well.
     */
    private boolean isCreateTableAsSelectAllowed() {
        return !pxcStrict && !gtidConsistencyEnforced;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace =



                        configuration.getTablespace() == null
                        ? ""
                        : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        String baselineMarker = "";
        if (baseline) {
            if (isCreateTableAsSelectAllowed()) {
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
                // Revert to regular insert, which unfortunately is not safe in concurrent scenarios
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
        String selectVersionOutput = DatabaseType.getSelectVersionOutput(rawMainJdbcConnection);
        if (databaseType == DatabaseType.MARIADB) {
            try {
                String productVersion = jdbcMetaData.getDatabaseProductVersion();
                return correctForAzureMariaDB(productVersion, selectVersionOutput);

            } catch (SQLException e) {
                throw new FlywaySqlException("Unable to determine MariaDB server version", e);
            }
        }
        MigrationVersion jdbcMetadataVersion = super.determineVersion();
        return correctForMySQLWithBadMetadata(jdbcMetadataVersion, selectVersionOutput);
    }

    /*
     * Azure Database for MySQL reports version numbers incorrectly - it claims to be 5.6 (the gateway
     * version) while the db itself is 5.7 or greater, visible from SELECT VERSION(). We work around this specific
     * case. This code should be simplified as soon as Azure is fixed.
     * https://docs.microsoft.com/en-us/azure/mysql/concepts-limits#current-known-issues
     * A similar issue applies to Percona, except there the metadata claims to be 5.5.
     */
    static MigrationVersion correctForMySQLWithBadMetadata(MigrationVersion jdbcMetadataVersion, String selectVersionOutput) {
        if (selectVersionOutput.compareTo("5.7") >= 0 && jdbcMetadataVersion.toString().compareTo("5.7") < 0) {
            LOG.debug("MySQL-based database - reporting v" + jdbcMetadataVersion.toString() +" in JDBC metadata but database actually v" + selectVersionOutput);
            return extractVersionFromString(selectVersionOutput, MYSQL_VERSION_PATTERN);
        }
        return jdbcMetadataVersion;
    }

    /*
     * Azure Database for MariaDB also reports version numbers incorrectly - it claims to be MySQL 5.6 (the gateway
     * version) while the db itself is something like 10.3.6-MariaDB-suffix, visible from SELECT VERSION().
     * This code should be simplified as soon as Azure is fixed.
     * https://docs.microsoft.com/en-us/azure/mysql/concepts-limits#current-known-issues
     * https://mariadb.com/kb/en/server-system-variables/#version
     */
    static MigrationVersion correctForAzureMariaDB(String jdbcMetadataVersion, String selectVersionOutput) {
        if (jdbcMetadataVersion.startsWith("5.6")) {
            LOG.debug("Azure MariaDB database - reporting v5.6 in JDBC metadata but database actually v" + selectVersionOutput);
            return extractVersionFromString(selectVersionOutput, MARIADB_VERSION_PATTERN, MARIADB_WITH_MAXSCALE_VERSION_PATTERN);
        }
        return extractVersionFromString(jdbcMetadataVersion, MARIADB_VERSION_PATTERN, MARIADB_WITH_MAXSCALE_VERSION_PATTERN);
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
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("5.1");
        if (databaseType == DatabaseType.MARIADB) {

            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.1", org.flywaydb.core.internal.license.Edition.ENTERPRISE);


            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.2", org.flywaydb.core.internal.license.Edition.PRO);

            recommendFlywayUpgradeIfNecessary("10.4");
        } else {

            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("5.7", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

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