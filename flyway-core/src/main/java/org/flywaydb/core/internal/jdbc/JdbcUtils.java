/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlUnableToConnectToDbException;
import org.flywaydb.core.internal.strategy.BackoffStrategy;
import org.flywaydb.core.internal.util.ExceptionUtils;

@CustomLog
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcUtils {
    /**
     * Opens a new connection from this DataSource.
     *
     * @param dataSource             The DataSource to obtain the connection from.
     * @param connectRetries         The maximum number of retries when attempting to connect to the database.
     * @param connectRetriesInterval The maximum time between retries in seconds.
     * @return The new connection.
     * @throws FlywayException when the connection could not be opened.
     */

    public static Connection openConnection(final DataSource dataSource,
        final int connectRetries,
        final int connectRetriesInterval)
        throws FlywayException {
        final BackoffStrategy backoffStrategy = new BackoffStrategy(1, 2, connectRetriesInterval);

        final Properties systemProperties = System.getProperties();
        if (!systemProperties.containsKey("polyglot.engine.WarnInterpreterOnly")) {
            systemProperties.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        }

        int retries = 0;
        while (true) {
            try {
                return dataSource.getConnection();
            } catch (final SQLException sqlException) {
                FlywaySqlException.throwFlywayExceptionIfPossible(sqlException, dataSource);

                if (++retries > connectRetries) {
                    throw new FlywaySqlUnableToConnectToDbException(sqlException, dataSource);
                }
                final Throwable rootCause = ExceptionUtils.getRootCause(sqlException);
                String message = "Connection error: " + sqlException.getMessage();
                if (rootCause != null && rootCause != sqlException && rootCause.getMessage() != null) {
                    message += "\n(Caused by " + rootCause.getMessage() + ")";
                }
                LOG.warn(message + "\nRetrying in " + backoffStrategy.peek() + " sec...");
                try {
                    Thread.sleep(backoffStrategy.next() * 1000L);
                } catch (final InterruptedException e1) {
                    throw new FlywaySqlUnableToConnectToDbException(sqlException, dataSource);
                }
            }
        }
    }

    /**
     * Safely closes this Connection. This method never fails.
     */
    public static void closeConnection(final Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (final Exception e) {
            LOG.error("Error while closing database Connection: " + e.getMessage(), e);
        }
    }

    /**
     * Safely closes this Statement. This method never fails.
     */
    public static void closeStatement(final Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (final SQLException e) {
            LOG.error("Error while closing JDBC Statement", e);
        }
    }

    /**
     * Safely closes this ResultSet. This method never fails.
     */
    public static void closeResultSet(final ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (final SQLException e) {
            LOG.error("Error while closing JDBC ResultSet", e);
        }
    }

    public static DatabaseMetaData getDatabaseMetaData(final Connection connection) {
        final DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = connection.getMetaData();
        } catch (final SQLException e) {
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
    public static String getDatabaseProductName(final DatabaseMetaData databaseMetaData) {
        try {
            final String databaseProductName = databaseMetaData.getDatabaseProductName();
            if (databaseProductName == null) {
                throw new FlywayException("Unable to determine database. Product name is null.");
            }

            return databaseProductName
                + " "
                + databaseMetaData.getDatabaseMajorVersion()
                + "."
                + databaseMetaData.getDatabaseMinorVersion();
        } catch (final SQLException e) {
            throw new FlywaySqlException("Error while determining database product name", e);
        }
    }

    /**
     * @return The version of the database product. Example: MariaDB 10.3, ...
     */
    public static String getDatabaseProductVersion(final DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDatabaseProductVersion();
        } catch (final SQLException e) {
            throw new FlywaySqlException("Error while determining database product version", e);
        }
    }

    public static String getDatabaseVersion(final DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDatabaseMajorVersion() + "." + databaseMetaData.getDatabaseMinorVersion();
        } catch (final SQLException e) {
            throw new FlywaySqlException("Error while determining database version", e);
        }
    }
}
