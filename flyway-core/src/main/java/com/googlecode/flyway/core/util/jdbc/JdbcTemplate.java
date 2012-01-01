/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.util.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of utility methods for querying the DB. Inspired by Spring's JdbcTemplate.
 */
public abstract class JdbcTemplate {
    /**
     * The DB connection to use.
     */
    private Connection connection;

    /**
     * Creates a new JdbcTemplate.
     *
     * @param connection The DB connection to use.
     */
    public JdbcTemplate(Connection connection) {
        this.connection = connection;
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
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            statement.setString(i + 1, params[i]);
        }
        ResultSet resultSet = statement.executeQuery();

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        while (resultSet.next()) {
            Map<String, String> rowMap = new HashMap<String, String>();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                rowMap.put(resultSet.getMetaData().getColumnLabel(i), resultSet.getString(i));
            }
            result.add(rowMap);
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
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            statement.setString(i + 1, params[i]);
        }
        ResultSet resultSet = statement.executeQuery();

        List<String> result = new ArrayList<String>();
        while (resultSet.next()) {
            result.add(resultSet.getString(1));
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
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            statement.setString(i + 1, params[i]);
        }
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
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
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            statement.setString(i + 1, params[i]);
        }
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
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
     * Executes this sql statement.
     *
     * @param sql    The statement to execute.
     * @param params The statement parameters.
     * @throws SQLException when the execution failed.
     */
    public void execute(String sql, Object... params) throws SQLException {
        PreparedStatement statement = prepareStatement(sql, params);
        statement.execute();
    }

    /**
     * Executes this update sql statement.
     *
     * @param sql    The statement to execute.
     * @param params The statement parameters.
     * @throws SQLException when the execution failed.
     */
    public void update(String sql, Object... params) throws SQLException {
        PreparedStatement statement = prepareStatement(sql, params);
        statement.executeUpdate();
    }

    /**
     * Creates a new prepared statement for this sql with these params.
     * @param sql The sql to execute.
     * @param params The params.
     * @return The new prepared statement.
     * @throws SQLException when the statement could not be prepared.
     */
    private PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                setNull(statement, i + 1);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else {
                statement.setString(i + 1, (String) params[i]);
            }
        }
        return statement;
    }

    /**
     * Sets the value of the parameter with this index to {@code null} in this PreparedStatement.
     *
     * @param preparedStatement The prepared statement whose parameter to set.
     * @param parameterIndex The index of the parameter to set.
     * @throws SQLException when the value could not be set.
     */
    protected abstract void setNull(PreparedStatement preparedStatement, int parameterIndex) throws SQLException;

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
        ResultSet resultSet = connection.createStatement().executeQuery(query);

        List<T> results = new ArrayList<T>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }
}
