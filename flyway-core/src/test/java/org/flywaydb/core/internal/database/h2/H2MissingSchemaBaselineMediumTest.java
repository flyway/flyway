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
package org.flywaydb.core.internal.database.h2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.api.logging.LogFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * H2 coverage for migrating when one configured schema already has objects and another does not exist.
 */
@Tag("flyway-core")
class H2MissingSchemaBaselineMediumTest {
    private static final String EMPTY_SCHEMAS_LOG =
        "All configured schemas are empty; a baseline marker will not be added";

    @Test
    void rejectsNonEmptySchemaWhenAnotherConfiguredSchemaIsMissing() throws Exception {
        final String url = memUrl("reject");
        createSchemaWithRow(url);
        final Path migrations = migrations(duplicateExistingTable(), "SELECT 1;");

        final Flyway flyway = flyway(url, migrations, false);
        final FlywayException exception = assertThrows(FlywayException.class, flyway::migrate);
        assertEquals(CoreErrorCode.NON_EMPTY_SCHEMA_WITHOUT_SCHEMA_HISTORY_TABLE, exception.getErrorCode());

        final FlywayException again = assertThrows(FlywayException.class, flyway::migrate);
        assertEquals(CoreErrorCode.NON_EMPTY_SCHEMA_WITHOUT_SCHEMA_HISTORY_TABLE, again.getErrorCode());

        assertEquals(1, count(url, "SELECT COUNT(*) FROM MAINTAIN.EXISTING_DATA"));
        assertFalse(schemaExists(url, "TMS"));
        assertFalse(history(url).stream().anyMatch(row -> "1".equals(row.version()) && "SQL".equals(row.type())));
    }

    @Test
    void baselinesExistingDataWhenAnotherConfiguredSchemaIsMissing() throws Exception {
        final String url = memUrl("baseline");
        createSchemaWithRow(url);
        final Path migrations = migrations(duplicateExistingTable(),
            "CREATE TABLE MAINTAIN.AFTER_BASELINE (ID INT PRIMARY KEY);");

        flyway(url, migrations, true).migrate();

        final List<HistoryRow> rows = history(url);
        assertTrue(rows.stream().anyMatch(row -> "2".equals(row.version()) && "BASELINE".equals(row.type())));
        assertFalse(rows.stream().anyMatch(row -> "1".equals(row.version())));
        assertEquals(1, count(url, "SELECT COUNT(*) FROM MAINTAIN.EXISTING_DATA"));
        assertTrue(tableExists(url, "MAINTAIN", "AFTER_BASELINE"));
    }

    @Test
    void doesNotBaselineWhenEveryConfiguredSchemaAlreadyExistsAndIsEmpty() throws Exception {
        final String url = memUrl("empty-existing");
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA MAINTAIN");
            statement.execute("CREATE SCHEMA TMS");
        }
        final Path migrations = migrations("CREATE TABLE MAINTAIN.CREATED (ID INT PRIMARY KEY);", "SELECT 1;");
        final RecordingLogCreator logs = new RecordingLogCreator();

        migrateWithLog(url, migrations, logs);

        assertFalse(history(url).stream().anyMatch(row -> "BASELINE".equals(row.type())));
        assertTrue(tableExists(url, "MAINTAIN", "CREATED"));
        assertTrue(logs.info.stream().anyMatch(message -> message.contains(EMPTY_SCHEMAS_LOG)));
    }

    @Test
    void doesNotBaselineWhenCreatingAMissingSchemaLeavesEverySchemaEmpty() throws Exception {
        final String url = memUrl("empty-missing");
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA MAINTAIN");
        }
        final Path migrations = migrations("CREATE TABLE MAINTAIN.CREATED (ID INT PRIMARY KEY);", "SELECT 1;");
        final RecordingLogCreator logs = new RecordingLogCreator();

        migrateWithLog(url, migrations, logs);

        assertFalse(history(url).stream().anyMatch(row -> "BASELINE".equals(row.type())));
        assertTrue(tableExists(url, "MAINTAIN", "CREATED"));
        assertTrue(schemaExists(url, "TMS"));
        assertFalse(logs.info.stream().anyMatch(message -> message.contains(EMPTY_SCHEMAS_LOG)));
    }

    @Test
    void createsSchemaAddedAfterHistoryAlreadyExists() throws Exception {
        final String url = memUrl("added-schema");
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA MAINTAIN");
        }
        final Path migrations = migrations("CREATE TABLE MAINTAIN.CREATED (ID INT PRIMARY KEY);", "SELECT 1;");

        Flyway.configure()
            .dataSource(url, "sa", "")
            .schemas("MAINTAIN")
            .table("history")
            .locations("filesystem:" + migrations.toAbsolutePath())
            .load()
            .migrate();

        Flyway.configure()
            .dataSource(url, "sa", "")
            .schemas("MAINTAIN", "TMS")
            .table("history")
            .locations("filesystem:" + migrations.toAbsolutePath())
            .load()
            .migrate();

        assertTrue(schemaExists(url, "TMS"));
        assertFalse(history(url).stream().anyMatch(row -> "BASELINE".equals(row.type())));
    }

    private static void migrateWithLog(final String url, final Path migrations, final RecordingLogCreator logs) {
        final Flyway flyway = flyway(url, migrations, true);
        LogFactory.setLogCreator(logs);
        try {
            flyway.migrate();
        } finally {
            LogFactory.setLogCreator(null);
        }
    }

    private static Flyway flyway(final String url, final Path migrations, final boolean baselineOnMigrate) {
        return Flyway.configure()
            .dataSource(url, "sa", "")
            .schemas("MAINTAIN", "TMS")
            .table("history")
            .locations("filesystem:" + migrations.toAbsolutePath())
            .baselineOnMigrate(baselineOnMigrate)
            .baselineVersion("2")
            .load();
    }

    private static String memUrl(final String name) {
        return "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1";
    }

    private static void createSchemaWithRow(final String url) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA MAINTAIN");
            statement.execute("CREATE TABLE MAINTAIN.EXISTING_DATA (ID INT PRIMARY KEY)");
            statement.execute("INSERT INTO MAINTAIN.EXISTING_DATA VALUES (42)");
        }
    }

    private static Path migrations(final String v1, final String v3) throws Exception {
        final Path directory = Files.createTempDirectory("flyway-4295-");
        Files.writeString(directory.resolve("V1__init.sql"), v1);
        Files.writeString(directory.resolve("V3__next.sql"), v3);
        return directory;
    }

    private static String duplicateExistingTable() {
        return "CREATE TABLE MAINTAIN.EXISTING_DATA (ID INT PRIMARY KEY);";
    }

    private static int count(final String url, final String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static boolean schemaExists(final String url, final String schema) throws SQLException {
        return count(url,
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + schema + "'") > 0;
    }

    private static boolean tableExists(final String url, final String schema, final String table) throws SQLException {
        return count(url,
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '"
                + schema
                + "' AND TABLE_NAME = '"
                + table
                + "'") > 0;
    }

    private static List<HistoryRow> history(final String url) throws SQLException {
        final List<HistoryRow> rows = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "SELECT \"version\", \"type\" FROM MAINTAIN.\"history\" ORDER BY \"installed_rank\"")) {
            while (result.next()) {
                rows.add(new HistoryRow(result.getString(1), result.getString(2)));
            }
        } catch (final SQLException exception) {
            if (exception.getErrorCode() == 42102 || "42S02".equals(exception.getSQLState())) {
                return rows;
            }
            throw exception;
        }
        return rows;
    }

    private record HistoryRow(String version, String type) {
    }

    private static final class RecordingLogCreator implements LogCreator {
        private final List<String> info = new ArrayList<>();

        @Override
        public Log createLogger(final Class<?> clazz) {
            return new Log() {
                @Override
                public void debug(final String message) {
                }

                @Override
                public void info(final String message) {
                    info.add(message);
                }

                @Override
                public void warn(final String message) {
                }

                @Override
                public void error(final String message) {
                }

                @Override
                public void error(final String message, final Exception exception) {
                }

                @Override
                public void notice(final String message) {
                }
            };
        }
    }
}
