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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;


import org.flywaydb.core.internal.util.ExceptionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Utility class for dealing with jdbc connections.
 */
public class JdbcConnectionFactory {
    private static final Log LOG = LogFactory.getLog(JdbcConnectionFactory.class);

    private final DataSource dataSource;
    private final int connectRetries;
    private final DatabaseType databaseType;
    private final String jdbcUrl;
    private final String driverInfo;
    private final String productName;

    private Connection firstConnection;
    private ConnectionInitializer connectionInitializer;
















    /**
     * Creates a new JDBC connection factory. This automatically opens a first connection which can be obtained via
     * a call to getConnection and which must be closed again to avoid leaking it.
     *
     * @param dataSource                 The dataSource to obtain the connection from.
     * @param connectRetries             The maximum number of retries when attempting to connect to the database.



     */
    public JdbcConnectionFactory(DataSource dataSource, int connectRetries



    ) {
        this.dataSource = dataSource;
        this.connectRetries = connectRetries;

        firstConnection = JdbcUtils.openConnection(dataSource, connectRetries);

        this.databaseType = DatabaseType.fromJdbcConnection(firstConnection);
        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(firstConnection);
        this.jdbcUrl = getJdbcUrl(databaseMetaData);
        this.driverInfo = getDriverInfo(databaseMetaData);
        this.productName = JdbcUtils.getDatabaseProductName(databaseMetaData);





    }

    public void setConnectionInitializer(ConnectionInitializer connectionInitializer) {
        this.connectionInitializer = connectionInitializer;
    }



























































    /**
     * @return The type of database this is.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * @return The JDBC url for these connections.
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDriverInfo() {
        return driverInfo;
    }

    public String getProductName() {
        return productName;
    }

    /**
     * Opens a new connection from this dataSource.
     *
     * @return The new connection.
     * @throws FlywayException when the connection could not be opened.
     */
    public Connection openConnection() throws FlywayException {
        Connection connection =
                firstConnection == null ? JdbcUtils.openConnection(dataSource, connectRetries) : firstConnection;
        firstConnection = null;

        if (connectionInitializer != null) {
            connectionInitializer.initialize(this, connection);
        }












        return connection;
    }

    public interface ConnectionInitializer {
        void initialize(JdbcConnectionFactory jdbcConnectionFactory, Connection connection);
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

    private static String getDriverInfo(DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDriverName() + " " + databaseMetaData.getDriverVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read database driver info: " + e.getMessage(), e);
        }
    }
}