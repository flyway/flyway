/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.MySQL.class)
@RunWith(Parameterized.class)
public class MySQLMigrationMediumTest extends MySQLMigrationTestCase {
    static final String JDBC_URL_MYSQL_55 = "jdbc:mysql://localhost:62030/flyway_db";
    static final String JDBC_URL_MYSQL_56 = "jdbc:mysql://localhost:62031/flyway_db";
    static final String JDBC_URL_MYSQL_57 = "jdbc:mysql://localhost:62032/flyway_db";
    static final String JDBC_URL_MYSQL_80 = "jdbc:mysql://localhost:62033/flyway_db";

    static final String JDBC_USER = "root";
    static final String JDBC_PASSWORD = "flywayPWD000";

    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_MYSQL_80},
                {JDBC_URL_MYSQL_57},
                {JDBC_URL_MYSQL_56},
                {JDBC_URL_MYSQL_55}
        });
    }

    public MySQLMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }

    @Test
    public void migrateWithNonExistingSchemaSetInPropertyButNotInUrl() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(createDataSource());
        flyway.setSchemas("non-existing-schema");
        flyway.setLocations(getBasedir());
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }

    @Test
    public void migrateWithExistingSchemaSetInPropertyButNotInUrl() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(createDataSource());
        flyway.setSchemas("test");
        flyway.setLocations(getBasedir());
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }
}
