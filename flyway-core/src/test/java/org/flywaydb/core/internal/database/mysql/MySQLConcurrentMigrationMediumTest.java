/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.flywaydb.core.internal.database.mysql.MySQLMigrationMediumTest.*;

/**
 * Test to demonstrate the migration functionality using MySQL.
 */
@Category(DbCategory.MySQL.class)
@RunWith(Parameterized.class)
public class MySQLConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    private final String jdbcUrl;
    private final boolean needsBaseline;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_MYSQL_80, true},
                {JDBC_URL_MYSQL_57, true},
                {JDBC_URL_MYSQL_56, true},
                {JDBC_URL_MYSQL_55, true}
        });
    }

    public MySQLConcurrentMigrationMediumTest(String jdbcUrl, boolean needsBaseline) {
        this.jdbcUrl = jdbcUrl;
        this.needsBaseline = needsBaseline;
    }

    @Override
    protected boolean needsBaseline() {
        return needsBaseline;
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }
}