/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import java.io.IOException;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.util.IOUtils;

/**
 * Collection of utility methods for querying the DB. Inspired by Spring's JdbcTemplate.
 */
public class JdbcTemplate {
    @Getter
    protected final Connection connection;
    /**
     * The type to assign to a null value.
     */
    protected final int nullType;

    public JdbcTemplate(final Connection connection, final DatabaseType databaseType) {
        this.connection = connection;
        this.nullType = databaseType.getNullType();
    }

    public JdbcTemplate(final Connection connection, final int nullType) {
        this.connection = connection;
        this.nullType = nullType;
    }

    public List<Map<String, String>> queryForList(final String query, final Object... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Map<String, String>> result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();

            result = new ArrayList<>();
            while (resultSet.next()) {
                final Map<String, String> rowMap = new LinkedHashMap<>();
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

    public List<String> queryForStringList(final String query, final String... params) throws SQLException {
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

    public int queryForInt(final String query, final String... params) throws SQLException {
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

    public long queryForLong(final String query, final String... params) throws SQLException {
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

    public boolean queryForBoolean(final String query, final String... params) throws SQLException {
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

    public String queryForString(final String query, final String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result = null;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    public Optional<InputStream> queryForBinaryStream(final String query, final Object... params) throws SQLException {
        final PreparedStatement statement = prepareStatement(query, params);
        final ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            final var snapshotStream = resultSet.getBinaryStream(1);
            return Optional.of(new ResultStream(snapshotStream, statement, resultSet));
        }

        JdbcUtils.closeResultSet(resultSet);
        JdbcUtils.closeStatement(statement);
        return Optional.empty();
    }

    /**
     * Executes this sql statement using a PreparedStatement.
     *
     * @param sql    The statement to execute.
     * @param params The statement parameters.
     * @throws SQLException when the execution failed.
     */
    public void execute(final String sql, final Object... params) throws SQLException {
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
    public Results executeStatement(final String sql) {
        final Results results = new Results();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.setEscapeProcessing(false);

            final boolean hasResults = statement.execute(sql);
            extractResults(results, statement, sql, hasResults);
            extractWarnings(results, statement);
        } catch (final SQLException e) {
            extractErrors(results, e);
        } finally {
            JdbcUtils.closeStatement(statement);
        }
        return results;
    }

    protected void extractWarnings(final Results results, final Statement statement) throws SQLException {
        SQLWarning warning = statement.getWarnings();
        while (warning != null) {
            final int code = warning.getErrorCode();
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

    public void extractErrors(final Results results, final SQLException e) {







        results.setException(e);
    }

    protected void extractResults(final Results results,
        final Statement statement,
        final String sql,
        boolean hasResults) throws SQLException {
        // retrieve all results to ensure all errors are detected
        int updateCount = -1;
        while (hasResults || (updateCount = statement.getUpdateCount()) != -1) {
            List<String> columns = null;
            List<List<String>> data = null;
            if (hasResults) {
                try (final ResultSet resultSet = statement.getResultSet()) {
                    columns = new ArrayList<>();
                    final ResultSetMetaData metadata = resultSet.getMetaData();
                    final int columnCount = metadata.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metadata.getColumnName(i));
                    }

                    data = new ArrayList<>();

                    while (resultSet.next()) {
                        final List<String> row = new ArrayList<>();
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
     * @param sql    The statement to execute.
     * @param params The statement parameters.
     * @throws SQLException when the execution failed.
     */
    public void update(final String sql, final Object... params) throws SQLException {

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
    protected PreparedStatement prepareStatement(final String sql, final Object[] params) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            final var parameterIndex = i + 1;
            final var parameterValue = params[i];
            if (parameterValue == null) {
                statement.setNull(parameterIndex, nullType);
            } else if (parameterValue instanceof final Integer integerValue) {
                statement.setInt(parameterIndex, integerValue);
            } else if (parameterValue instanceof final Boolean booleanValue) {
                statement.setBoolean(parameterIndex, booleanValue);
            } else if (parameterValue instanceof final String stringValue) {
                statement.setString(parameterIndex, stringValue);
            } else if (parameterValue instanceof final ChronoZonedDateTime<?> zonedDateTimeValue) {
                statement.setTimestamp(parameterIndex, new Timestamp(zonedDateTimeValue.toInstant().toEpochMilli()));
            } else if (parameterValue instanceof final InputStream inputStream) {
                statement.setBinaryStream(parameterIndex, inputStream);
            } else if (parameterValue == JdbcNullTypes.StringNull) {
                statement.setNull(parameterIndex, nullType);
            } else if (parameterValue == JdbcNullTypes.IntegerNull) {
                statement.setNull(parameterIndex, nullType);
            } else if (parameterValue == JdbcNullTypes.BooleanNull) {
                statement.setNull(parameterIndex, nullType);
            } else {
                throw new FlywayException("Unhandled object of type '"
                    + parameterValue.getClass().getName()
                    + "'. "
                    + "Please contact support or leave an issue on GitHub.");
            }
        }
        return statement;
    }

    /**
     * Executes this query and map the results using this row mapper.
     *
     * @param sql       The query to execute.
     * @param rowMapper The row mapper to use.
     * @param <T>       The type of the result objects.
     * @return The list of results.
     * @throws SQLException when the query failed to execute.
     */
    public <T> List<T> query(final String sql, final RowMapper<? extends T> rowMapper, final Object... params)
        throws SQLException {
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

    /**
     * Executes this batch of SQL statements.
     *
     * @param sqlBatch The batch of statements.
     */
    public Results executeBatch(final Iterable<String> sqlBatch) {
        final Results results = new Results();
        Statement statement = null;
        final StringBuilder sb = new StringBuilder();
        try {
            statement = connection.createStatement();
            for (final String sql : sqlBatch) {
                sb.append(sql);
                statement.addBatch(sql);
            }
            try {
                for (final int intResult : statement.executeBatch()) {
                    results.addResult(new Result(intResult, null, null, sb.toString()));
                }
            } catch (final BatchUpdateException e) {
                for (final int intResult : e.getUpdateCounts()) {
                    results.addResult(new Result(intResult, null, null, sb.toString()));
                }
                extractErrors(results, e);
            } finally {
                extractWarnings(results, statement);
            }
        } catch (final SQLException e) {
            extractErrors(results, e);
        } finally {
            JdbcUtils.closeStatement(statement);
        }
        return results;
    }

    @RequiredArgsConstructor
    @CustomLog
    private static class ResultStream extends InputStream {
        private final InputStream blobInputStream;
        private final PreparedStatement preparedStatement;
        private final ResultSet resultSet;

        @Override
        public int read() throws IOException {
            return blobInputStream.read();
        }

        @Override
        public void close() {
            IOUtils.close(blobInputStream);
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(preparedStatement);
        }
    }
}
