/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using CockroachDB.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.CockroachDB.class)
public class CockroachDBMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                getUrl("flyway_db"), "flyway", "");
    }

    private String getUrl(String database) {
        return "jdbc:postgresql://127.0.0.1:62000/" + database + "?sslmode=disable";
    }

    @Override
    protected String getBasedir() {
        return "migration/database/cockroachdb/sql/default";
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Ignore("Flaky due to CockroachDB bug")
    @Test
    public void root() {
        flyway.setDataSource(new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                getUrl(""), "root", ""));
        flyway.setSchemas("mydatabase");
        flyway.setLocations(getBasedir());
        flyway.migrate();
        flyway.clean();
    }

    @Test
    public void index() {
        flyway.setLocations("migration/database/cockroachdb/sql/index");
        flyway.migrate();
    }

    @Test
    public void cleanUnknown() {
        flyway.setSchemas("non-existant");
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/database/cockroachdb/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    @Test(expected = FlywayException.class)
    public void warning() {
        flyway.setLocations("migration/database/postgresql/sql/warning");
        flyway.migrate();
        // Log should contain "This is a warning"
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
