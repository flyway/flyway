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
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL database.
 */
public class MySQLDatabase extends Database<MySQLConnection> {
    private static final Log LOG = LogFactory.getLog(MySQLDatabase.class);

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
    }

    @Override
    protected MySQLConnection getConnection(Connection connection



    ) {
        return new MySQLConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    public final void ensureSupported() {
        MigrationVersion version = MigrationVersion.fromVersion(majorVersion + "." + minorVersion);
        boolean isMariaDB;
        boolean isMariaDriver;
        try {
            isMariaDB = jdbcMetaData.getDatabaseProductVersion().contains("MariaDB");
            isMariaDriver = jdbcMetaData.getDriverName().contains("MariaDB");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine database product version and driver", e);
        }
        String productName = isMariaDB ? "MariaDB" : "MySQL";

        if (version.compareTo(MigrationVersion.fromVersion("5.1")) < 0) {
            throw new FlywayDbUpgradeRequiredException(productName, version.getVersion(), "5.1");
        }

        if (version.compareTo(MigrationVersion.fromVersion("5.5")) < 0) {
            throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException(
                    isMariaDB ? "MariaDB" : "Oracle", productName, version.getVersion());
        }

        if (isMariaDB) {
            if (version.compareTo(MigrationVersion.fromVersion("10.3")) > 0) {
                recommendFlywayUpgrade(productName, version.getVersion());
            }
        } else {



                if (isMariaDriver) {
                    LOG.warn("You are connected to a MySQL database using the MariaDB driver." +
                            " This is known to cause issues." +
                            " An upgrade to Oracle's MySQL JDBC driver is highly recommended.");
                }



            if (version.compareTo(MigrationVersion.fromVersion("8.0")) > 0) {
                recommendFlywayUpgrade(productName, version.getVersion());
            }
        }
    }

    @Override
    protected SqlStatementBuilderFactory createSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer



    ) {
        return new MySQLSqlStatementBuilderFactory(placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "mysql";
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
        return true;
    }

}