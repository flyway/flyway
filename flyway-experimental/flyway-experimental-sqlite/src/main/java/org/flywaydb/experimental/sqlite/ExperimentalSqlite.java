/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-sqlite
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.experimental.sqlite;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.MetaData;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.database.sqlite.SQLiteParser;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.AsciiTable;
import org.sqlite.SQLiteDataSource;

@CustomLog
public class ExperimentalSqlite implements ExperimentalDatabase {
    private Connection connection;
    private final ArrayList<String> batch = new ArrayList<>();

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public String getDatabaseType() {
        return "SQLite";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        final String url = environment.getUrl() + "?allowMultiQueries=true";
        final SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        final int connectRetries = environment.getConnectRetries() != null ? environment.getConnectRetries() : 0;
        final int connectRetriesInterval = environment.getConnectRetriesInterval() != null ? environment.getConnectRetriesInterval() : 0;
        connection = JdbcUtils.openConnection(dataSource, connectRetries, connectRetriesInterval);
    }
    
    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return SQLiteParser::new;
    }
    
    @Override
    public void addToBatch(final String executionUnit) {
        batch.add(executionUnit);
    }

    @Override
    public void doExecuteBatch() {
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
    public void doExecute(final String executionUnit, final boolean outputQueryResults) {
        try (final Statement statement = connection.createStatement()) {
            final boolean hasResult = statement.execute(executionUnit);
            parseResults(hasResult, statement, outputQueryResults);
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public MetaData getDatabaseMetaData() {
        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        final String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        final String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);
        return new MetaData(databaseProductName, databaseProductVersion, ConnectionType.JDBC, null);
    }

    @Override
    public void createSchemaHistoryTable(final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final String createSql = "CREATE TABLE \"" + tableName + "\" (\n" +
                "    \"installed_rank\" INT NOT NULL PRIMARY KEY,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now')),\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ");\n";
            statement.executeUpdate(createSql);
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }
    
    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            return resultSet.next();
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final String querySql = "SELECT installed_rank"
                + ", version"
                + ", description"
                + ", type"
                + ", script"
                + ", checksum"
                + ", installed_on"
                + ", installed_by"
                + ", execution_time"
                + ", success"
                + " FROM \"" + tableName + "\""
                + " WHERE installed_rank > 0"
                + " ORDER BY installed_rank";
            final ResultSet resultSet = statement.executeQuery(querySql);
            final ArrayList<SchemaHistoryItem> items = new ArrayList<>();
            while(resultSet.next()) {
                items.add(SchemaHistoryItem
                              .builder()
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
    public void appendSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final StringBuilder insertSql = new StringBuilder().append("INSERT INTO \"")
                .append(tableName).append("\" (installed_rank, ")
                .append(item.getVersion() == null ? "" : "version,")
                .append(" description, type, script, checksum, installed_by, execution_time, success) VALUES (")
                .append(item.getInstalledRank())
                .append(", ");
            if (item.getVersion() != null) {
                insertSql.append("'")
                    .append(item.getVersion())
                    .append("', ");
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
                .append(item.isSuccess())
                .append(")");
            statement.execute(insertSql.toString());
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public String getCurrentSchema() {
        return "main";
    }

    @Override
    public String getDefaultSchema(Configuration configuration) {
        return "main";
    }

    @Override
    public Boolean allSchemasEmpty(final String[] schemas) {
        return isSchemaEmpty(null);
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master");
            resultSet.next();
            return resultSet.getInt(1) == 0;
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isSchemaExists(final String schema) {
        return true;
    }

    @Override
    public void createSchemas(final String... schemas) {
        //SQLite does not support creating schemas
    }

    @Override
    public String getCurrentUser() {
        try {
            return JdbcUtils.getDatabaseMetaData(connection).getUserName();
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void startTransaction() {
        try {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("BEGIN TRANSACTION;");
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void commitTransaction() {
        try {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("COMMIT TRANSACTION;");
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("ROLLBACK TRANSACTION;");
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void doCleanSchema(String schema) {
        final boolean foreignKeysEnabled = queryBoolean("PRAGMA foreign_keys");

        // Get all tables and views
        try {
            final List<String> viewNames = queryForStringList("SELECT tbl_name FROM sqlite_master WHERE type='view'");
            for (final String viewName : viewNames) {
                try (final Statement statement = connection.createStatement()) {
                    statement.execute("DROP VIEW " + quote(viewName));
                }
            }
            final List<String> tableNames = queryForStringList("SELECT tbl_name FROM sqlite_master WHERE type='table'")
                .stream()
                .filter(tableName -> !tableName.equals("sqlite_sequence"))
                .toList();
            for (final String tableName : tableNames) {
                try (final Statement statement = connection.createStatement()) {
                    String dropSql = "DROP TABLE " + quote(tableName);
                    if (foreignKeysEnabled) {
                        // #2417: Disable foreign keys before dropping tables to avoid constraint violation errors
                        dropSql = "PRAGMA foreign_keys = OFF; " + dropSql + "; PRAGMA foreign_keys = ON";
                    }
                    statement.execute(dropSql);
                }
            }
            if (queryBoolean("SELECT count(tbl_name) FROM sqlite_master WHERE type='table' AND tbl_name='sqlite_sequence'")) {
                try (final Statement statement = connection.createStatement()) {
                    statement.execute("DELETE FROM sqlite_sequence");
                }
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void removeFailedSchemaHistoryItems(final String tableName) {
        try {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM " + quote(tableName) + " WHERE " + quote("success") + " = 0");
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void updateSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        try {
            final String sql = new StringBuilder().append("UPDATE ")
                .append(quote(tableName))
                .append(" SET ")
                .append(quote("description"))
                .append("=? , ")
                .append(quote("type"))
                .append("=? , ")
                .append(quote("checksum"))
                .append("=?")
                .append(" WHERE ")
                .append(quote("installed_rank"))
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

    private boolean queryBoolean(final String sql) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    public List<String> queryForStringList(String query) throws SQLException {
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
