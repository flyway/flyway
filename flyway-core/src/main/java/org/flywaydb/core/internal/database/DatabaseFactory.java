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
        String databaseProductName = getDatabaseProductName(connection);
        if (printInfo) {
            LOG.info("Database: " + getJdbcUrl(connection) + " (" + databaseProductName + ")");
        }

        Database database = createDatabase(configuration, connection, databaseProductName



        );
        database.ensureSupported();

        if (!database.supportsChangingCurrentSchema() && configuration.getSchemas().length > 0) {
            LOG.warn(databaseProductName + " does not support setting the schema for the current session. " +
                    "Default schema will NOT be changed to " + configuration.getSchemas()[0] + " !");
        }

        return database;
    }

    private static Database createDatabase(Configuration configuration, Connection connection, String databaseProductName



    ) {
        if (databaseProductName.startsWith("Apache Derby")) {
            return new DerbyDatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("SQLite")) {
            return new SQLiteDatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("H2")) {
            return new H2Database(configuration, connection



            );
        }
        if (databaseProductName.contains("HSQL Database Engine")) {
            return new HSQLDBDatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
            return new SQLServerDatabase(configuration, connection



            );
        }
        if (databaseProductName.contains("MySQL")) {
            // For regular MySQL, MariaDB and Google Cloud SQL.
            // Google Cloud SQL returns different names depending on the environment and the SDK version.
            //   ex.: Google SQL Service/MySQL
            return new MySQLDatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("Oracle")) {
            return new OracleDatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("PostgreSQL 8")) {
            if (RedshiftDatabase.isRedshift(connection)) {
                return new RedshiftDatabase(configuration, connection



                );
            }
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
            if (CockroachDBDatabase.isCockroachDB(connection)) {
                return new CockroachDBDatabase(configuration, connection



                );
            }
            return new PostgreSQLDatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("DB2")) {
            return new DB2Database(configuration, connection



            );
        }
        if (databaseProductName.startsWith("ASE")) {
            return new SybaseASEDatabase(configuration, connection, false



            );
        }
        if (databaseProductName.startsWith("Adaptive Server Enterprise")) {
            return new SybaseASEDatabase(configuration, connection, true



            );
        }
        if (databaseProductName.startsWith("HDB")) {
            return new SAPHANADatabase(configuration, connection



            );
        }
        if (databaseProductName.startsWith("Informix")) {
            return new InformixDatabase(configuration, connection



            );
        }
        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the Jdbc Url for this connection.
     *
     * @param connection The Jdbc connection.
     * @return The Jdbc Url.
     */

    private static String getJdbcUrl(Connection connection) {
        try {
            return filterUrl(connection.getMetaData().getURL());
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
     * @param connection The connection to use to query the database.
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private static String getDatabaseProductName(Connection connection) {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            if (databaseMetaData == null) {
                throw new FlywayException("Unable to read database metadata while it is null!");
            }

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