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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of utility methods for querying the DB. Inspired by Spring's JdbcTemplate.
 */
public class JdbcTemplate {
    protected final Connection connection;
    /**
     * The type to assign to a null value.
     */
    protected final int nullType;

    public JdbcTemplate(Connection connection) {
        this(connection, DatabaseTypeRegister.getDatabaseTypeForConnection(connection));
    }

    public JdbcTemplate(Connection connection, DatabaseType databaseType) {
        this.connection = connection;
        this.nullType = databaseType.getNullType();
    }

    public Connection getConnection() {
        return connection;
    }

    public List<Map<String, String>> queryForList(String query, Object... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Map<String, String>> result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();

            result = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> rowMap = new LinkedHashMap<>();
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

    public List<String> queryForStringList(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<String> result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();

            result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    public int queryForInt(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        int result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getInt(1);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    public long queryForLong(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        long result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getLong(1);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    public boolean queryForBoolean(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        boolean result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getBoolean(1);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    public String queryForString(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = prepareStatement(query, params);
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
     * Executes this sql statement using a PreparedStatement.
     *
     * @param sql The statement to execute.
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
     * @return the results of the execution.
     */
    public Results executeStatement(String sql) {
        Results results = new Results();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.setEscapeProcessing(false);

            boolean hasResults = statement.execute(sql);
            extractResults(results, statement, sql, hasResults);
            extractWarnings(results, statement);
        } catch (final SQLException e) {
            extractErrors(results, e);
        } finally {
            JdbcUtils.closeStatement(statement);
        }
        return results;
    }

    private void extractWarnings(Results results, Statement statement) throws SQLException {
        SQLWarning warning = statement.getWarnings();
        while (warning != null) {
            int code = warning.getErrorCode();
            String state = warning.getSQLState();
            String message = warning.getMessage();

            if (state == null) {
                state = "";
            }

            if (message == null) {
                message = "";
            }

            results.addWarning(new WarningImpl(code, state, message));
            warning = warning.getNextWarning();
        }
    }

    public void extractErrors(Results results, SQLException e) {







        results.setException(e);
    }

    private void extractResults(Results results, Statement statement, String sql, boolean hasResults) throws SQLException {
        // retrieve all results to ensure all errors are detected
        int updateCount = -1;
        while (hasResults || (updateCount = statement.getUpdateCount()) != -1) {
            List<String> columns = null;
            List<List<String>> data = null;
            if (hasResults) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    columns = new ArrayList<>();
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columnCount = metadata.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metadata.getColumnName(i));
                    }

                    data = new ArrayList<>();

                    while (resultSet.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.add(resultSet.getString(i));
                        }
                        data.add(row);
                    }
                }
            }
            results.addResult(new Result(updateCount, columns, data, sql));
            hasResults = statement.getMoreResults();
        }
    }

    /**
     * Executes this update sql statement.
     *
     * @param sql The statement to execute.
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
     * @param sql The sql to execute.
     * @param params The params.
     * @return The new prepared statement.
     * @throws SQLException when the statement could not be prepared.
     */
    protected PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                statement.setNull(i + 1, nullType);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) params[i]);
            } else if (params[i] instanceof String) {
                statement.setString(i + 1, params[i].toString());
            } else if (params[i] == JdbcNullTypes.StringNull) {
                statement.setNull(i + 1, nullType);
            } else if (params[i] == JdbcNullTypes.IntegerNull) {
                statement.setNull(i + 1, nullType);
            } else if (params[i] == JdbcNullTypes.BooleanNull) {
                statement.setNull(i + 1, nullType);
            } else {
                throw new FlywayException("Unhandled object of type '" + params[i].getClass().getName() + "'. " +
                                                  "Please contact support or leave an issue on GitHub.");
            }
        }
        return statement;
    }

    /**
     * Executes this query and map the results using this row mapper.
     *
     * @param sql The query to execute.
     * @param rowMapper The row mapper to use.
     * @param <T> The type of the result objects.
     * @return The list of results.
     * @throws SQLException when the query failed to execute.
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<T> results;
        try {
            statement = prepareStatement(sql, params);
            resultSet = statement.executeQuery();

            results = new ArrayList<>();
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