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
package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of utility methods for querying the DB. Inspired by Spring's JdbcTemplate.
 */
public class JdbcTemplate {
    private static final Log LOG = LogFactory.getLog(JdbcTemplate.class);

    /**
     * The DB connection to use.
     */
    private final Connection connection;

    /**
     * The type to assign to a null value.
     */
    private final int nullType;

    /**
     * Creates a new JdbcTemplate.
     *
     * @param connection The DB connection to use.
     */
    public JdbcTemplate(Connection connection) {
        this.connection = connection;
        this.nullType = Types.NULL;
    }

    /**
     * Creates a new JdbcTemplate.
     *
     * @param connection The DB connection to use.
     * @param nullType   The type to assign to a null value.
     */
    public JdbcTemplate(Connection connection, int nullType) {
        this.connection = connection;
        this.nullType = nullType;
    }

    /**
     * @return The DB connection to use.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Executes this query with these parameters against this connection.
     *
     * @param query  The query to execute.
     * @param params The query parameters.
     * @return The query results.
     * @throws SQLException when the query execution failed.
     */
    public List<Map<String, String>> queryForList(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Map<String, String>> result;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();

            result = new ArrayList<Map<String, String>>();
            while (resultSet.next()) {
                Map<String, String> rowMap = new HashMap<String, String>();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    rowMap.put(resultSet.getMetaData().getColumnLabel(i), resultSet.getString(i));
                }
                result.add(rowMap);
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }


        return result;
    }

    /**
     * Executes this query with these parameters against this connection.
     *
     * @param query  The query to execute.
     * @param params The query parameters.
     * @return The query results as a list of strings.
     * @throws SQLException when the query execution failed.
     */
    public List<String> queryForStringList(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<String> result;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();

            result = new ArrayList<String>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    /**
     * Executes this query with these parameters against this connection.
     *
     * @param query  The query to execute.
     * @param params The query parameters.
     * @return The query result.
     * @throws SQLException when the query execution failed.
     */
    public int queryForInt(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        int result;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();
            result = 0;
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    /**
     * Executes this query with these parameters against this connection.
     *
     * @param query  The query to execute.
     * @param params The query parameters.
     * @return The query result.
     * @throws SQLException when the query execution failed.
     */
    public boolean queryForBoolean(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        boolean result;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getBoolean(1);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    /**
     * Executes this query with these parameters against this connection.
     *
     * @param query  The query to execute.
     * @param params The query parameters.
     * @return The query result.
     * @throws SQLException when the query execution failed.
     */
    public String queryForString(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();
            result = null;
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    /**
     * Retrieves the database metadata for the connection associated with this JdbcTemplate.
     *
     * @return The database metadata.
     * @throws SQLException when the database metadata could not be retrieved.
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }

    /**
     * Executes this sql statement using a PreparedStatement.
     *
     * @param sql    The statement to execute.
     * @param params The statement parameters.
     * @throws SQLException when the execution failed.
     */
    public void execute(String sql, Object... params) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(sql, params);
            statement.execute();
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    /**
     * Executes this sql statement using an ordinary Statement.
     *
     * @param sql The statement to execute.
     * @throws SQLException when the execution failed.
     */
    public void executeStatement(String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.setEscapeProcessing(false);
            boolean hasResults = false;
            try {
                hasResults = statement.execute(sql);
            } finally {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") SQLWarning warning = statement.getWarnings();
                while (warning != null) {
                    if ("00000".equals(warning.getSQLState())) {
                        LOG.info("DB: " + warning.getMessage());
                    } else {
                        LOG.warn("DB: " + warning.getMessage()
                                + " (SQL State: " + warning.getSQLState() + " - Error Code: " + warning.getErrorCode() + ")");
                    }
                    warning = warning.getNextWarning();
                }
                // retrieve all results to ensure all errors are detected
                int updateCount = -1;
                while (hasResults || (updateCount = statement.getUpdateCount()) != -1) {
                    if (updateCount != -1) {
                        LOG.debug("Update Count: " + updateCount);
                    }
                    hasResults = statement.getMoreResults();
                }
            }
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    /**
     * Executes this update sql statement.
     *
     * @param sql    The statement to execute.
     * @param params The statement parameters.
     * @throws SQLException when the execution failed.
     */
    public void update(String sql, Object... params) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(sql, params);
            statement.executeUpdate();
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    /**
     * Creates a new prepared statement for this sql with these params.
     *
     * @param sql    The sql to execute.
     * @param params The params.
     * @return The new prepared statement.
     * @throws SQLException when the statement could not be prepared.
     */
    private PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                statement.setNull(i + 1, nullType);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) params[i]);
            } else {
                statement.setString(i + 1, (String) params[i]);
            }
        }
        return statement;
    }

    /**
     * Executes this query and map the results using this row mapper.
     *
     * @param query     The query to execute.
     * @param rowMapper The row mapper to use.
     * @param <T>       The type of the result objects.
     * @return The list of results.
     * @throws SQLException when the query failed to execute.
     */
    public <T> List<T> query(String query, RowMapper<T> rowMapper) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;

        List<T> results;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            results = new ArrayList<T>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return results;
    }
}