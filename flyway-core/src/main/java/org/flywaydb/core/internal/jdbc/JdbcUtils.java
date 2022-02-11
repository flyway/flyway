/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import lombok.CustomLog;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.strategy.BackoffStrategy;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import javax.sql.DataSource;
import java.sql.*;

@CustomLog
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcUtils {
    /**
     * Opens a new connection from this DataSource.
     *
     * @param dataSource The DataSource to obtain the connection from.
     * @param connectRetries The maximum number of retries when attempting to connect to the database.
     * @param connectRetriesInterval The maximum time between retries in seconds.
     * @return The new connection.
     * @throws FlywayException when the connection could not be opened.
     */
    public static Connection openConnection(DataSource dataSource, int connectRetries, int connectRetriesInterval) throws FlywayException {
        BackoffStrategy backoffStrategy = new BackoffStrategy(1, 2, connectRetriesInterval);
        int retries = 0;
        while (true) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                if ("08S01".equals(e.getSQLState()) && e.getMessage().contains("This driver is not configured for integrated authentication")) {
                    throw new FlywaySqlException("Unable to obtain connection from database" + getDataSourceInfo(dataSource, true) + ": " + e.getMessage() +
                                                         "\nTo setup integrated authentication see " + FlywayDbWebsiteLinks.WINDOWS_AUTH, e);
                } else if (e.getSQLState() == null && e.getMessage().contains("MSAL4J")) {
                    throw new FlywaySqlException("Unable to obtain connection from database" + getDataSourceInfo(dataSource, false) + ": " + e.getMessage() +
                                                         "\nYou need to install some extra drivers in order for interactive authentication to work." +
                                                         "\nFor instructions, see " + FlywayDbWebsiteLinks.AZURE_ACTIVE_DIRECTORY, e);
                }

                if (++retries > connectRetries) {
                    throw new FlywaySqlException("Unable to obtain connection from database" + getDataSourceInfo(dataSource, false) + ": " + e.getMessage(), e);
                }
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                String message = "Connection error: " + e.getMessage();
                if (rootCause != null && rootCause != e && rootCause.getMessage() != null) {
                    message += "\n(Caused by " + rootCause.getMessage() + ")";
                }
                LOG.warn(message + "\nRetrying in " + backoffStrategy.peek() + " sec...");
                try {
                    Thread.sleep(backoffStrategy.next() * 1000L);
                } catch (InterruptedException e1) {
                    throw new FlywaySqlException("Unable to obtain connection from database" + getDataSourceInfo(dataSource, false) + ": " + e.getMessage(), e);
                }
            }
        }
    }

    private static String getDataSourceInfo(DataSource dataSource, boolean suppressNullUserMessage) {
        if (!(dataSource instanceof DriverDataSource)) {
            return "";
        }
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        String user = driverDataSource.getUser();
        String info = " (" + DatabaseTypeRegister.redactJdbcUrl(driverDataSource.getUrl()) + ")";
        if (user != null || !suppressNullUserMessage) {
            info += " for user '" + user + "'";
        }
        return info;
    }

    /**
     * Safely closes this Connection. This method never fails.
     */
    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (Exception e) {
            LOG.error("Error while closing database Connection: " + e.getMessage(), e);
        }
    }

    /**
     * Safely closes this Statement. This method never fails.
     */
    public static void closeStatement(Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException e) {
            LOG.error("Error while closing JDBC Statement", e);
        }
    }

    /**
     * Safely closes this ResultSet. This method never fails.
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
            LOG.error("Error while closing JDBC ResultSet", e);
        }
    }

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
     * @return The name of the database product. Example: Oracle, MySQL, ...
     */
    public static String getDatabaseProductName(DatabaseMetaData databaseMetaData) {
        try {
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            if (databaseProductName == null) {
                throw new FlywayException("Unable to determine database. Product name is null.");
            }

            return databaseProductName + " " + databaseMetaData.getDatabaseMajorVersion() + "." + databaseMetaData.getDatabaseMinorVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while determining database product name", e);
        }
    }

    /**
     * @return The version of the database product. Example: MariaDB 10.3, ...
     */
    public static String getDatabaseProductVersion(DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while determining database product version", e);
        }
    }
}