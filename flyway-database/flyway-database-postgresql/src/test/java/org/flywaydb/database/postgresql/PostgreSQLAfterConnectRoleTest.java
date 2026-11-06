/*-
 * ========================LICENSE_START=================================
 * flyway-database-postgresql
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
package org.flywaydb.database.postgresql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Requires a disposable PostgreSQL database and an administrator able to create roles and schemas.
 */
@EnabledIfEnvironmentVariable(named = "FLYWAY_POSTGRESQL_TEST_URL", matches = ".+")
class PostgreSQLAfterConnectRoleTest {
    @TempDir
    Path scripts;

    private Connection admin;
    private String login;
    private String owner;
    private String schema;
    private String password;

    @BeforeEach
    void setUp() throws SQLException {
        final String suffix = UUID.randomUUID().toString().replace("-", "");
        login = "login_" + suffix;
        owner = "owner_" + suffix;
        schema = "schema_" + suffix;
        password = UUID.randomUUID().toString();
        admin = DriverManager.getConnection(System.getenv("FLYWAY_POSTGRESQL_TEST_URL"),
            System.getenv().getOrDefault("FLYWAY_POSTGRESQL_TEST_USER", "postgres"),
            System.getenv().getOrDefault("FLYWAY_POSTGRESQL_TEST_PASSWORD", ""));
        execute("CREATE ROLE " + owner + " NOLOGIN");
        execute("CREATE ROLE " + login + " LOGIN NOINHERIT PASSWORD '" + password + "'");
        execute("GRANT " + owner + " TO CURRENT_USER");
        execute("GRANT " + owner + " TO " + login);
        execute("GRANT CONNECT ON DATABASE " + databaseName() + " TO " + login);
        execute("CREATE SCHEMA " + schema + " AUTHORIZATION " + owner);
        execute("GRANT USAGE, CREATE ON SCHEMA " + schema + " TO " + login);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (admin != null) {
            try (final Connection cleanup = admin) {
                execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
                execute("REVOKE CONNECT ON DATABASE " + databaseName() + " FROM " + login);
                execute("DROP ROLE IF EXISTS " + login);
                execute("DROP ROLE IF EXISTS " + owner);
            }
        }
    }

    @Test
    void afterConnectRolePersistsAcrossCallbacksAndMigrations() throws Exception {
        script("afterConnect__a.sql", "SET ROLE " + owner + ";");
        script("afterConnect__b.sql", "SELECT 1;");

        migrateAs(owner, login);
    }

    @Test
    void nonTransactionalAfterConnectRolePersistsAcrossCallbacksAndMigrations() throws Exception {
        script("afterConnect__a.sql", "SET ROLE " + owner + ";");
        script("afterConnect__a.sql.conf", "executeInTransaction=false");
        script("afterConnect__b.sql", "SELECT 1;");

        migrateAs(owner, login);
    }

    @Test
    void transactionLocalRoleIsNotAdoptedAfterCallbackCommit() throws Exception {
        script("afterConnect__a.sql", "SET LOCAL ROLE " + owner + ";");
        script("afterConnect__b.sql", "SELECT 1;");

        migrateAs(login, owner);
    }

    @Test
    void noCallbackRestoresLoginRoleBetweenMigrations() throws Exception {
        migrateAs(login, owner);
    }

    private void migrateAs(final String expected, final String changed) throws IOException, SQLException {
        script("V1__first.sql", assertRole(expected) + "CREATE TABLE first_probe (id int); SET ROLE " + changed + ";");
        script("V2__second.sql", assertRole(expected) + "CREATE TABLE second_probe (id int);");
        assertEquals(2, Flyway.configure()
            .dataSource(System.getenv("FLYWAY_POSTGRESQL_TEST_URL"), login, password)
            .defaultSchema(schema)
            .locations("filesystem:" + scripts.toAbsolutePath())
            .load().migrate().migrationsExecuted);
        try (final var statement = admin.createStatement();
             final var result = statement.executeQuery("SELECT tableowner FROM pg_tables WHERE schemaname = '"
                 + schema + "' AND tablename IN ('first_probe', 'second_probe')")) {
            int count = 0;
            while (result.next()) {
                assertEquals(expected, result.getString(1));
                count++;
            }
            assertEquals(2, count);
        }
    }

    private String assertRole(final String expected) {
        return "DO $$ BEGIN IF current_user <> '" + expected + "' OR current_role <> '" + expected
            + "' THEN RAISE EXCEPTION 'Unexpected role: %', current_role; END IF; END $$;";
    }

    private void script(final String name, final String sql) throws IOException {
        Files.writeString(scripts.resolve(name), sql);
    }

    private void execute(final String sql) throws SQLException {
        try (final var statement = admin.createStatement()) {
            statement.execute(sql);
        }
    }

    private String databaseName() throws SQLException {
        return "\"" + admin.getCatalog().replace("\"", "\"\"") + "\"";
    }
}
