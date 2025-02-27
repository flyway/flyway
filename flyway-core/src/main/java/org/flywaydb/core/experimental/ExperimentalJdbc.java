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
package org.flywaydb.core.experimental;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.util.AsciiTable;

public abstract class ExperimentalJdbc implements ExperimentalDatabase{
    protected Connection connection;
    protected final ArrayList<String> batch = new ArrayList<>();
    protected MetaData metaData;

    @Override
    public boolean canCreateJdbcDataSource() {
        return true;
    }

    @Override
    public void addToBatch(final String executionUnit) {
        batch.add(executionUnit);
    }

    @Override
    public void doExecute(final String executionUnit, final boolean outputQueryResults) {
        try (final Statement statement = connection.createStatement()) {
            final boolean hasResult = statement.execute(executionUnit);
            parseResults(hasResult, statement, outputQueryResults);
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void doExecuteBatch() {
        if (batch.isEmpty()) {
            return;
        }
        try (final Statement statement = connection.createStatement()) {
            for (final String sql : batch) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
            batch.clear();
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public int getBatchSize() {
        return batch.size();
    }

    @Override
    public String getCurrentUser() {
        try {
            return JdbcUtils.getDatabaseMetaData(connection).getUserName();
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isClosed() {
        try {
            return connection != null && connection.isClosed();
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (!isClosed()) {
            connection.close();
        }
    }

    protected boolean queryBoolean(final String sql) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    public List<String> queryForStringList(final String query) throws SQLException {
        ResultSet resultSet = null;

        List<String> result;
        try (final Statement statement = connection.createStatement()) {
            resultSet = statement.executeQuery(query);

            result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return result;
    }

    private void parseResults(boolean hasResults, final Statement statement, final boolean outputQueryResult) throws SQLException {
        if (outputQueryResult) {
            while (hasResults || (statement.getUpdateCount()) != -1) {
                final List<String> columns;
                final List<List<String>> data;
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
                    outputResult(new Result(-1, columns, data, ""));
                }
                hasResults = statement.getMoreResults();
            }
        }
    }

    private void outputResult(final Result result) {
        if (result.columns() != null && !result.columns().isEmpty()) {
            LOG.info(new AsciiTable(result.columns(), result.data(),
                true, "", "No rows returned").render());
        }
    }
}
