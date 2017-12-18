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
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@Category(DbCategory.MariaDB.class)
@RunWith(Parameterized.class)
public class MariaDBMigrationMediumTest extends MySQLMigrationTestCase {
    private static final String JDBC_URL_MARIADB_55 = "jdbc:mariadb://localhost:62020/flyway_db";
    private static final String JDBC_URL_MARIADB_100 = "jdbc:mariadb://localhost:62021/flyway_db";
    private static final String JDBC_URL_MARIADB_101 = "jdbc:mariadb://localhost:62022/flyway_db";
    private static final String JDBC_URL_MARIADB_102 = "jdbc:mariadb://localhost:62023/flyway_db";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "flywayPWD000";

    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_MARIADB_102},
                {JDBC_URL_MARIADB_101},
                {JDBC_URL_MARIADB_100},
                {JDBC_URL_MARIADB_55}
        });
    }

    public MariaDBMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }
}
