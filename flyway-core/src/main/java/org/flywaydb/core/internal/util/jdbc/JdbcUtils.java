/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;

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
                throw new FlywayException("Unable to obtain database connection");
            }
            return connection;
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to obtain database connection", e);
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
}
