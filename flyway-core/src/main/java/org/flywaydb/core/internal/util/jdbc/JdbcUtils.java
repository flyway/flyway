/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
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
     * @param dataSource The dataSource to obtain the connection from.
     * @return The new connection.
     * @throws FlywayException when the connection could not be opened.
     */
    public static Connection openConnection(DataSource dataSource) throws FlywayException {
        try {
            Connection connection = dataSource.getConnection();
            if (connection == null) {
                throw new FlywayException("Unable to obtain Jdbc connection from DataSource");
            }
            return connection;
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to obtain Jdbc connection from DataSource", e);
        }
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
        } catch (SQLException e) {
            LOG.error("Error while closing Jdbc connection", e);
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
            LOG.error("Error while closing Jdbc statement", e);
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
            LOG.error("Error while closing Jdbc resultSet", e);
        }
    }
}
