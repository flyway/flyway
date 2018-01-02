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

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
     * @throws SQLException when the database metadata could not be retrieved.
     */
    public JdbcTemplate(Connection connection) throws SQLException {
        this(connection, Types.NULL);
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
                Map<String, String> rowMap = new LinkedHashMap<String, String>();
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
            resultSet.next();
            result = resultSet.getInt(1);
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
    public List<Result> executeStatement(ContextImpl errorContext, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.setEscapeProcessing(false);
            boolean hasResults;
            try {
                hasResults = statement.execute(sql);
            } finally {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") SQLWarning warning = statement.getWarnings();
                while (warning != null) {
                    errorContext.addWarning(new WarningImpl(warning.getErrorCode(), warning.getSQLState(), warning.getMessage()));
                    warning = warning.getNextWarning();
                }
            }
            return extractResults(statement, hasResults);
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    private List<Result> extractResults(Statement statement, boolean hasResults) throws SQLException {
        List<Result> results = new ArrayList<Result>();

        // retrieve all results to ensure all errors are detected
        int updateCount = -1;
        while (hasResults || (updateCount = statement.getUpdateCount()) != -1) {
            // [pro]
            List<String> columns = null;
            List<List<String>> data = null;
            if (hasResults) {
                ResultSet resultSet = statement.getResultSet();

                try {
                    columns = new ArrayList<String>();
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columnCount = metadata.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metadata.getColumnName(i));
                    }

                    data = new ArrayList<List<String>>();
                    while (resultSet.next()) {
                        List<String> row = new ArrayList<String>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.add(resultSet.getString(i));
                        }
                        data.add(row);
                    }
                } finally {
                    resultSet.close();
                }
            }
            // [/pro]
            results.add(new Result(updateCount
                    // [pro]
                    , columns, data
                    // [/pro]
            ));
            hasResults = statement.getMoreResults();
        }

        return results;
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
                statement.setString(i + 1, params[i].toString());
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
