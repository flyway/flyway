/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.DriverManager;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Derby.
 */
@Category(DbCategory.Derby.class)
public class DerbyMigrationMediumTest extends MigrationTestCase {
    static {
        System.setProperty("derby.stream.error.field", "java.lang.System.err");
    }

    @Override
    public void tearDown() throws Exception {
        try {
            new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:derby:memory:flyway_db;drop=true", "", "", null).getConnection();
        } catch (FlywayException e) {
            //OK, expected error 08006. See http://db.apache.org/derby/docs/dev/devguide/cdevdvlpinmemdb.html
        }

        super.tearDown();
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:derby:memory:flyway_db;create=true", "", "", null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void bitdata() {
        flyway.setLocations("migration/database/derby/sql/bitdata");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void trigger() {
        flyway.setLocations("migration/database/derby/sql/trigger");
        flyway.migrate();

        // Fails if triggers aren't cleaned properly
        flyway.clean();
    }

    @Test
    public void validateOnMigrate() {
        flyway.setLocations("migration/sql");
        flyway.setSchemas("non-existant");
        flyway.setValidateOnMigrate(true);
        flyway.migrate();
    }

    @Test
    public void testFlyway1331() {
        try {
            Flyway flyway = new Flyway();
            flyway.setDataSource("jdbc:derby:memory:fw1331db;create=true", "sa", "sa");
            flyway.setLocations("migration/sql");
            flyway.setBaselineOnMigrate(true);
            flyway.migrate();
        } finally {
            try {
                JdbcUtils.closeConnection(DriverManager.getConnection("jdbc:derby:memory:fw1331db;shutdown=true", "sa", "sa"));
            } catch (Exception e) {
                // Suppress
            }
        }
    }
}