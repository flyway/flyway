/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-sqlite
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
package org.flywaydb.experimental.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.ExperimentalJdbc;
import org.flywaydb.core.internal.database.sqlite.SQLiteParser;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

@CustomLog
public class ExperimentalSqlite extends ExperimentalJdbc <String>  {

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public List<String> supportedVerbs() {
        return List.of("info", "validate", "migrate", "clean", "undo", "baseline", "repair");
    }

    @Override
    public String getDatabaseType() {
        return "SQLite";
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return SQLiteParser::new;
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            return resultSet.next();
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    protected boolean supportsSchema() {
        return false;
    }

    @Override
    public String getSchemaPlaceHolder() {
        return "main";
    }

    @Override
    public Boolean allSchemasEmpty(final String[] schemas) {
        return isSchemaEmpty(null);
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT tbl_name FROM sqlite_master WHERE type='table'");
            final List<String> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            final List<String> ignoredSystemTableNames = List.of("android_metadata", "sqlite_sequence");

            return ignoredSystemTableNames.containsAll(result);
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
            final List<String> tableNames = queryForStringList("SELECT tbl_name FROM sqlite_master WHERE type='table'").stream()
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
            if (queryBoolean(
                "SELECT count(tbl_name) FROM sqlite_master WHERE type='table' AND tbl_name='sqlite_sequence'")) {
                try (final Statement statement = connection.createStatement()) {
                    statement.execute("DELETE FROM sqlite_sequence");
                }
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void doDropSchema(final String schema) {

    }

    @Override
    protected boolean supportsCatalog() {
        return false;
    }
}
