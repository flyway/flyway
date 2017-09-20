/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.cockroachdb;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExternalResource;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using CockroachDB.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.CockroachDB.class)
public class CockroachDBMigrationMediumTest extends MigrationTestCase {
    private static String jdbcUrl;
    private static String jdbcUser;
    private static String jdbcPassword;

    @ClassRule
    public static ExternalResource initCockroachDB() {
        return new ExternalResource() {
            @Override
            protected void before() throws Throwable {
                jdbcUrl = customProperties.getProperty("cockroachdb.url", "jdbc:postgresql://127.0.0.1:26257/flyway_db?sslmode=disable");
                jdbcUser = customProperties.getProperty("cockroachdb.user", "flyway");
                jdbcPassword = customProperties.getProperty("cockroachdb.password", "");
            }
        };
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, jdbcUser, jdbcPassword, null);
    }

    @Override
    protected String getBasedir() {
        return "migration/dbsupport/cockroachdb/sql/default";
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void index() throws Exception {
        flyway.setLocations("migration/dbsupport/cockroachdb/sql/index");
        flyway.migrate();
    }

    @Test
    public void cleanUnknown() throws Exception {
        flyway.setSchemas("non-existant");
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/cockroachdb/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    @Test(expected = FlywayException.class)
    public void warning() {
        flyway.setLocations("migration/dbsupport/postgresql/sql/warning");
        flyway.migrate();
        // Log should contain "This is a warning"
    }

    @Ignore("Not necessary")
    @Override
    public void upgradeMetadataTableTo40Format() throws Exception {
    }

    @Ignore("Only works with root user")
    @Override
    public void setCurrentSchema() throws Exception {
    }

    @Ignore("Broken in CockroachDB")
    @Override
    public void failedMigration() throws Exception {
    }

    @Ignore("Broken in CockroachDB")
    @Override
    public void migrateMultipleSchemas() throws Exception {
    }
}