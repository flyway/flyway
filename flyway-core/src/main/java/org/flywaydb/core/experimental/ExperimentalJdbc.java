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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.util.AsciiTable;

public abstract class ExperimentalJdbc <T> extends AbstractExperimentalDatabase <T> {
    protected Connection connection;

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        final int connectRetries = environment.getConnectRetries() != null ? environment.getConnectRetries() : 0;
        final int connectRetriesInterval = environment.getConnectRetriesInterval() != null
            ? environment.getConnectRetriesInterval()
            : 0;
        connection = JdbcUtils.openConnection(configuration.getDataSource(), connectRetries, connectRetriesInterval);
        initializeConnectionType(environment, configuration);
        currentSchema = getDefaultSchema(configuration);
        metaData = getDatabaseMetaData();
    }

    @Override
    public boolean canCreateJdbcDataSource() {
        return true;
    }

    @Override
    public void doExecute(final T executionUnit, final boolean outputQueryResults) {
        try (final Statement statement = connection.createStatement()) {
            final boolean hasResult = statement.execute((String) executionUnit);
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
    public String getCurrentUser() {
        try {
            return JdbcUtils.getDatabaseMetaData(connection).getUserName();
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public final MetaData getDatabaseMetaData() {
        if (this.metaData != null) {
            return metaData;
        }

        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        final String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        final String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        String databaseName = null;
        try {
            databaseName = supportsCatalog() ? connection.getCatalog() :
            supportsSchema() ? getCurrentSchema() : null;
        } catch (SQLException ignored) {
        }

        return new MetaData(databaseProductName, databaseProductVersion, getConnectionType(), databaseName);
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

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String table) {
        try (final Statement statement = connection.createStatement()) {
            final String querySql = "SELECT " + doQuote("installed_rank")
                + ", " + doQuote("version")
                + ", " + doQuote("description")
                + ", " + doQuote("type")
                + ", " + doQuote("script")
                + ", " + doQuote("checksum")
                + ", " + doQuote("installed_on")
                + ", " + doQuote("installed_by")
                + ", " + doQuote("execution_time")
                + ", " + doQuote("success")
                + " FROM "
                + getTableNameWithSchema(table)
                + " WHERE "+ doQuote("installed_rank") + " >= 0"
                + " ORDER BY " + doQuote("installed_rank");
            final ResultSet resultSet = statement.executeQuery(querySql);
            final ArrayList<SchemaHistoryItem> items = new ArrayList<>();
            while (resultSet.next()) {
                items.add(SchemaHistoryItem.builder()
                    .installedRank(resultSet.getInt("installed_rank"))
                    .version(resultSet.getString("version"))
                    .description(resultSet.getString("description"))
                    .type(resultSet.getString("type"))
                    .script(resultSet.getString("script"))
                    .checksum(resultSet.getInt("checksum"))
                    .installedOn(resultSet.getTimestamp("installed_on").toLocalDateTime())
                    .installedBy(resultSet.getString("installed_by"))
                    .executionTime(resultSet.getInt("execution_time"))
                    .success(resultSet.getBoolean("success"))
                    .build());
            }
            return new SchemaHistoryModel(items);
        } catch (final SQLException e) {
            return new SchemaHistoryModel();
        }
    }

    @Override
    public void createSchemaHistoryTable(final Configuration configuration) {
        try (final Statement statement = connection.createStatement()) {
            final String createSql = "CREATE TABLE "
                + getTableNameWithSchema(configuration.getTable())
                + " (\n"
                + doQuote("installed_rank") + " INT NOT NULL PRIMARY KEY,\n"
                + doQuote("version") + " VARCHAR(50),\n"
                + doQuote("description") + " VARCHAR(200) NOT NULL,\n"
                + doQuote("type") + " VARCHAR(20) NOT NULL,\n"
                + doQuote("script") + " VARCHAR(1000) NOT NULL,\n"
                + doQuote("checksum") + " INT,\n"
                + doQuote("installed_by") + " VARCHAR(100) NOT NULL,\n"
                + doQuote("installed_on") + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now')),\n"
                + doQuote("execution_time") + " INT NOT NULL,\n"
                + doQuote("success") + " BOOLEAN NOT NULL\n"
                + " );\n";
            statement.executeUpdate(createSql);
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void appendSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final StringBuilder insertSql = new StringBuilder().append("INSERT INTO ")
                .append(getTableNameWithSchema(tableName))
                .append(" (")
                .append(doQuote("installed_rank")  + ", ")
                .append(item.getVersion() == null ? "" : doQuote("version") + ", ")
                .append(doQuote("description")  + ", ")
                .append(doQuote("type")  + ", ")
                .append(doQuote("script")  + ", ")
                .append(doQuote("checksum")  + ", ")
                .append(doQuote("installed_by")  + ", ")
                .append(doQuote("execution_time")  + ", ")
                .append(doQuote("success"))
                .append(")");

            insertSql.append(" VALUES (")
                .append(item.getInstalledRank())
                .append(", ");
            if (item.getVersion() != null) {
                insertSql.append("'").append(item.getVersion()).append("', ");
            }
            insertSql.append("'")
                .append(item.getDescription())
                .append("', ")
                .append("'")
                .append(item.getType())
                .append("', ")
                .append("'")
                .append(item.getScript())
                .append("', ")
                .append(item.getChecksum())
                .append(", ")
                .append("'")
                .append(item.getInstalledBy() == null ? "" : item.getInstalledBy())
                .append("', ")
                .append(item.getExecutionTime())
                .append(", ")
                .append(supportsBoolean() ? item.isSuccess() : item.isSuccess() ? "1" : "0")
                .append(")");
            statement.execute(insertSql.toString());
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void removeFailedSchemaHistoryItems(final String tableName) {
        try {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM " + getTableNameWithSchema(tableName) + " WHERE " + doQuote("success") + " = 0");
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void updateSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        try {
            final String sql = new StringBuilder().append("UPDATE ")
                .append(getTableNameWithSchema(tableName))
                .append(" SET ")
                .append(doQuote("description"))
                .append("=? , ")
                .append(doQuote("type"))
                .append("=? , ")
                .append(doQuote("checksum"))
                .append("=?")
                .append(" WHERE ")
                .append(doQuote("installed_rank"))
                .append("=?")
                .toString();
            try (final PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, item.getDescription());
                statement.setString(2, item.getType());
                statement.setInt(3, item.getChecksum());
                statement.setInt(4, item.getInstalledRank());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public final String getDefaultSchema(final Configuration configuration) {
        if (!supportsSchema()) {
            return getSchemaPlaceHolder();
        }

        final String schema = ConfigUtils.getCalculatedDefaultSchema(configuration);
        if (schema == null) {
            try {
                return connection.getSchema();
            } catch (SQLException e) {
                throw new FlywayException(e);
            }
        }
        return schema;
    }

    protected String getTableNameWithSchema(final String table) {
        return quote(getCurrentSchema(), table);
    }

    protected boolean supportsSchema() {
        return true;
    }

    protected String getSchemaPlaceHolder() {
        return null;
    }

    protected boolean supportsBoolean() {
        return true;
    }

    protected boolean supportsCatalog() {
        return true;
    }

    protected void initializeConnectionType(final ResolvedEnvironment environment, final Configuration configuration) {
        connectionType = ConnectionType.JDBC;
    }
}
