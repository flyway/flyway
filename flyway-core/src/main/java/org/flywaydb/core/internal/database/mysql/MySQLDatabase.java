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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * MySQL database.
 */
public class MySQLDatabase extends Database<MySQLConnection> {
    private static final Log LOG = LogFactory.getLog(MySQLDatabase.class);

    /**
     * Whether this is a Percona XtraDB Cluster in strict mode.
     */
    private final boolean pxcStrict;

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public MySQLDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );

        pxcStrict = isRunningInPerconaXtraDBClusterWithStrictMode(connection);
    }

    static boolean isRunningInPerconaXtraDBClusterWithStrictMode(Connection connection) {
        try {
            if ("ENFORCING".equals(new JdbcTemplate(connection).queryForString(
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
    protected SqlScript getCreateScript(Map<String, String> placeholders) {
        Parser parser = new MySQLParser(new FluentConfiguration().placeholders(placeholders));
        return new ParserSqlScript(parser, getRawCreateScript(), false);
    }

    @Override
    protected LoadableResource getRawCreateScript() {
        String tablespace =



                        configuration.getTablespace() == null
                        ? ""
                        : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        return new StringResource("CREATE TABLE `${schema}`.`${table}` (\n" +
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
                "    -- Add the primary key as part of the CREATE TABLE statement in case `innodb_force_primary_key` is enabled\n" +
                "    CONSTRAINT `${table}_pk`PRIMARY KEY (`installed_rank`)\n" +
                ")" + tablespace + " ENGINE=InnoDB;\n" +
                "\n" +
                "CREATE INDEX `${table}_s_idx` ON `${schema}`.`${table}` (`success`);");
    }

    @Override
    protected MySQLConnection getConnection(Connection connection



    ) {
        return new MySQLConnection(configuration, this, connection, originalAutoCommit



        );
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
    public SqlScript createSqlScript(LoadableResource resource, boolean mixed



    ) {
        return new ParserSqlScript(new MySQLParser(configuration), resource, mixed);
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