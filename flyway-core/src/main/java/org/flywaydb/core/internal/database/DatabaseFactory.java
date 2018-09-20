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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBDatabase;
import org.flywaydb.core.internal.database.db2.DB2Database;
import org.flywaydb.core.internal.database.derby.DerbyDatabase;
import org.flywaydb.core.internal.database.h2.H2Database;
import org.flywaydb.core.internal.database.hsqldb.HSQLDBDatabase;
import org.flywaydb.core.internal.database.informix.InformixDatabase;
import org.flywaydb.core.internal.database.mysql.MySQLDatabase;
import org.flywaydb.core.internal.database.oracle.OracleDatabase;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabase;
import org.flywaydb.core.internal.database.redshift.RedshiftDatabase;
import org.flywaydb.core.internal.database.saphana.SAPHANADatabase;
import org.flywaydb.core.internal.database.sqlite.SQLiteDatabase;
import org.flywaydb.core.internal.database.sqlserver.SQLServerDatabase;
import org.flywaydb.core.internal.database.sybasease.SybaseASEDatabase;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Factory for obtaining the correct Database instance for the current connection.
 */
public class DatabaseFactory {
    private static final Log LOG = LogFactory.getLog(DatabaseFactory.class);

    /**
     * Prevent instantiation.
     */
    private DatabaseFactory() {
        //Do nothing
    }

    /**
     * Initializes the appropriate Database class for the database product used by the data source.
     *
     * @param configuration The Flyway configuration.
     * @param printInfo     Where the DB info should be printed in the logs.
     * @return The appropriate Database class.
     */
    public static Database createDatabase(Configuration configuration, boolean printInfo



    ) {
        OracleDatabase.enableTnsnamesOraSupport();

        Connection connection = JdbcUtils.openConnection(configuration.getDataSource(), configuration.getConnectRetries());
        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
            if (!originalAutoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to turn on auto-commit for the connection", e);
        }

        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        if (printInfo) {
            LOG.info("Database: " + getJdbcUrl(databaseMetaData) + " (" + databaseProductName + ")");
            LOG.debug("Driver  : " + getDriverInfo(databaseMetaData));
        }

        DatabaseType databaseType = DatabaseType.fromJdbcConnection(connection);

        Database database = createDatabase(databaseType, configuration, connection, originalAutoCommit



        );
        database.ensureSupported();

        if (!database.supportsChangingCurrentSchema() && configuration.getSchemas().length > 0) {
            LOG.warn(databaseProductName + " does not support setting the schema for the current session. " +
                    "Default schema will NOT be changed to " + configuration.getSchemas()[0] + " !");
        }

        return database;
    }

    private static String getDriverInfo(DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDriverName() + " " + databaseMetaData.getDriverVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read database driver info: " + e.getMessage(), e);
        }
    }

    private static Database createDatabase(DatabaseType databaseType, Configuration configuration,
                                           Connection connection, boolean originalAutoCommit



    ) {
        switch (databaseType) {
            case COCKROACHDB:
                return new CockroachDBDatabase(configuration, connection, originalAutoCommit



                );
            case DB2:
                return new DB2Database(configuration, connection, originalAutoCommit



                );
            case DERBY:
                return new DerbyDatabase(configuration, connection, originalAutoCommit



                );
            case H2:
                return new H2Database(configuration, connection, originalAutoCommit



                );
            case HSQLDB:
                return new HSQLDBDatabase(configuration, connection, originalAutoCommit



                );
            case INFORMIX:
                return new InformixDatabase(configuration, connection, originalAutoCommit



                );
            case MYSQL:
                return new MySQLDatabase(configuration, connection, originalAutoCommit



                );
            case ORACLE:
                return new OracleDatabase(configuration, connection, originalAutoCommit



                );
            case POSTGRESQL:
                return new PostgreSQLDatabase(configuration, connection, originalAutoCommit



                );
            case REDSHIFT:
                return new RedshiftDatabase(configuration, connection, originalAutoCommit



                );
            case SQLITE:
                return new SQLiteDatabase(configuration, connection, originalAutoCommit



                );
            case SAPHANA:
                return new SAPHANADatabase(configuration, connection, originalAutoCommit



                );
            case SQLSERVER:
                return new SQLServerDatabase(configuration, connection, originalAutoCommit



                );
            case SYBASEASE_JCONNECT:
            case SYBASEASE_JTDS:
                return new SybaseASEDatabase(configuration, connection, originalAutoCommit



                );
            default:
                throw new FlywayException("Unsupported Database: " + databaseType.name());
        }
    }

    /**
     * Retrieves the Jdbc Url for this connection.
     *
     * @param databaseMetaData The Jdbc connection metadata.
     * @return The Jdbc Url.
     */

    private static String getJdbcUrl(DatabaseMetaData databaseMetaData) {
        String url;
        try {
            url = databaseMetaData.getURL();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the JDBC connection URL!", e);
        }
        if (url == null) {
            return "";
        }
        return filterUrl(url);
    }

    /**
     * Filter out parameters to avoid including passwords, etc.
     *
     * @param url The raw url.
     * @return The filtered url.
     */
    static String filterUrl(String url) {
        int questionMark = url.indexOf("?");
        if (questionMark >= 0 && !url.contains("?databaseName=")) {
            url = url.substring(0, questionMark);
        }
        url = url.replaceAll("://.*:.*@", "://");
        return url;
    }
}