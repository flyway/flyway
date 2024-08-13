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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.MetaData;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.sqlite.SQLiteDataSource;

public class ExperimentalSqlite implements ExperimentalDatabase {
    private Connection connection;
    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public void initialize(final ResolvedEnvironment environment) throws SQLException {
        final String url = environment.getUrl();
        final SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        
        final int connectRetries = environment.getConnectRetries() != null ? environment.getConnectRetries() : 0;
        final int connectRetriesInterval = environment.getConnectRetriesInterval() != null ? environment.getConnectRetriesInterval() : 0;
        connection = JdbcUtils.openConnection(dataSource, connectRetries, connectRetriesInterval);
    }

    @Override
    public MetaData getDatabaseMetaData() {
        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        final String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        final String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);
        return new MetaData(databaseProductName, databaseProductVersion, ConnectionType.JDBC);
    }

    @Override
    public void createSchemaHistoryTable(final String tableName) {
        try {
            final Statement statement = connection.createStatement();
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
        try {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            return resultSet.next();
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String tableName) {
        try {
            final Statement statement = connection.createStatement();
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
            final ArrayList<SchemaHistoryItem> items = new ArrayList<SchemaHistoryItem>();
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
    public Boolean allSchemasEmpty() {
        try {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master");
            resultSet.next();
            return resultSet.getInt(1) == 0;
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }
}
