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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.ExceptionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for dealing with jdbc connections.
 */
public class JdbcUtils {
    private static final Log LOG = LogFactory.getLog(JdbcUtils.class);

    /**
     * Prevents instantiation.
     */
    private JdbcUtils() {
        //Do nothing
    }

    /**
     * Opens a new connection from this dataSource.
     *
     * @param dataSource     The dataSource to obtain the connection from.
     * @param connectRetries The maximum number of retries when attempting to connect to the database.
     * @return The new connection.
     * @throws FlywayException when the connection could not be opened.
     */
    public static Connection openConnection(DataSource dataSource, int connectRetries) throws FlywayException {
        int retries = 0;
        while (true) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                if (++retries > connectRetries) {
                    throw new FlywaySqlException("Unable to obtain connection from database"
                            + getDataSourceInfo(dataSource) + ": " + e.getMessage(), e);
                }
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                String msg = "Connection error: " + e.getMessage();
                if (rootCause != null && rootCause != e && rootCause.getMessage() != null) {
                    msg += " (Caused by " + rootCause.getMessage() + ")";
                }
                LOG.warn(msg + " Retrying in 1 sec...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    throw new FlywaySqlException("Unable to obtain connection from database"
                            + getDataSourceInfo(dataSource) + ": " + e.getMessage(), e);
                }
            }
        }
    }

    private static String getDataSourceInfo(DataSource dataSource) {
        if (!(dataSource instanceof DriverDataSource)) {
            return "";
        }
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        return " (" + driverDataSource.getUrl() + ") for user '" + driverDataSource.getUser() + "'";
    }

    /**
     * Safely closes this connection. This method never fails.
     *
     * @param connection The connection to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (Exception e) {
            LOG.error("Error while closing database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Safely closes this statement. This method never fails.
     *
     * @param statement The statement to close.
     */
    public static void closeStatement(Statement statement) {
        if (statement == null) {
            return;
        }

        try {
            statement.close();
        } catch (SQLException e) {
            LOG.error("Error while closing JDBC statement", e);
        }
    }

    /**
     * Safely closes this resultSet. This method never fails.
     *
     * @param resultSet The resultSet to close.
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
            LOG.error("Error while closing JDBC resultSet", e);
        }
    }

    /**
     * Retrieves the database metadata for this connection.
     *
     * @param connection The connection to use to query the database.
     * @return The database metadata.
     */
    public static DatabaseMetaData getDatabaseMetaData(Connection connection) {
        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = connection.getMetaData();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read database connection metadata: " + e.getMessage(), e);
        }
        if (databaseMetaData == null) {
            throw new FlywayException("Unable to read database connection metadata while it is null!");
        }
        return databaseMetaData;
    }

    /**
     * Retrieves the name of the database product.
     *
     * @param databaseMetaData The connection metadata to use to query the database.
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    public static String getDatabaseProductName(DatabaseMetaData databaseMetaData) {
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