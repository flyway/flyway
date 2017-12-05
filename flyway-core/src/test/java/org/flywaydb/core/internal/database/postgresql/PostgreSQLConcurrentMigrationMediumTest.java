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
package org.flywaydb.core.internal.database.postgresql;

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

import static org.flywaydb.core.internal.database.postgresql.PostgreSQLMigrationMediumTest.*;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@Category(DbCategory.PostgreSQL.class)
@RunWith(Parameterized.class)
public class PostgreSQLConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_POSTGRESQL_100},
                {JDBC_URL_POSTGRESQL_96},
                {JDBC_URL_POSTGRESQL_95},
                {JDBC_URL_POSTGRESQL_94},
                {JDBC_URL_POSTGRESQL_93},
                {JDBC_URL_POSTGRESQL_92}
        });
    }

    public PostgreSQLConcurrentMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }

    protected String getBasedir() {
        return "migration/database/postgresql/sql/concurrent";
    }

    @Override
    protected boolean isMixed() {
        // V1_1__View.sql has both a SELECT pg_sleep and CREATE INDEX CONCURRENTLY to help
        // to reproduce the deadlock which can occur with the use of advisory locks.
        // See #1654 for details.
        return true;
    }
}