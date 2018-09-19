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
import org.flywaydb.core.internal.database.Cache.CacheDatabase;
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
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

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

        Connection connection = JdbcUtils.openConnection(configuration.getDataSource());
        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
            if (!originalAutoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to turn on auto-commit for the connection", e);
        }

        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = connection.getMetaData();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read database connection metadata: " + e.getMessage(), e);
        }
        if (databaseMetaData == null) {
            throw new FlywayException("Unable to read database connection metadata while it is null!");
        }

        String databaseProductName = getDatabaseProductName(databaseMetaData);
        if (printInfo) {
            LOG.info("Database: " + getJdbcUrl(databaseMetaData) + " (" + databaseProductName + ")");
            LOG.debug("Driver  : " + getDriverInfo(databaseMetaData));
        }

        Database database = createDatabase(configuration, connection, originalAutoCommit, databaseProductName



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

    private static Database createDatabase(Configuration configuration, Connection connection,
                                           boolean originalAutoCommit,
                                           String databaseProductName



    ) {
        if (databaseProductName.startsWith("Apache Derby")) {
            return new DerbyDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("SQLite")) {
            return new SQLiteDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("H2")) {
            return new H2Database(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.contains("HSQL Database Engine")) {
            return new HSQLDBDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
            return new SQLServerDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.contains("MySQL")) {
            // For regular MySQL, MariaDB and Google Cloud SQL.
            // Google Cloud SQL returns different names depending on the environment and the SDK version.
            //   ex.: Google SQL Service/MySQL
            return new MySQLDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("Oracle")) {
            return new OracleDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("PostgreSQL 8")) {
            if (RedshiftDatabase.isRedshift(connection)) {
                return new RedshiftDatabase(configuration, connection, originalAutoCommit



                );
            }
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
            if (CockroachDBDatabase.isCockroachDB(connection)) {
                return new CockroachDBDatabase(configuration, connection, originalAutoCommit



                );
            }
            return new PostgreSQLDatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("DB2")) {
            return new DB2Database(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("ASE")) {
            return new SybaseASEDatabase(configuration, connection, originalAutoCommit, false



            );
        }
        if (databaseProductName.startsWith("Adaptive Server Enterprise")) {
            return new SybaseASEDatabase(configuration, connection, originalAutoCommit, true



            );
        }
        if (databaseProductName.startsWith("HDB")) {
            return new SAPHANADatabase(configuration, connection, originalAutoCommit



            );
        }
        if (databaseProductName.startsWith("Informix")) {
            return new InformixDatabase(configuration, connection, originalAutoCommit



            );
        }
        if(databaseProductName.startsWith("Cache")) {
            return new CacheDatabase(configuration, connection, originalAutoCommit);

        }

        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the Jdbc Url for this connection.
     *
     * @param databaseMetaData The Jdbc connection metadata.
     * @return The Jdbc Url.
     */

    private static String getJdbcUrl(DatabaseMetaData databaseMetaData) {
        try {
            return filterUrl(databaseMetaData.getURL());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the Jdbc connection Url!", e);
        }
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

    /**
     * Retrieves the name of the database product.
     *
     * @param databaseMetaData The connection metadata to use to query the database.
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private static String getDatabaseProductName(DatabaseMetaData databaseMetaData) {
        try {
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            if (databaseProductName == null) {
                throw new FlywayException("Unable to determine database. Product name is null.");
            }

            int databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
            int databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();

            return databaseProductName + " " + databaseMajorVersion + "." + databaseMinorVersion;
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while determining database product name", e);
        }
    }
}