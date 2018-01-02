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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

import static org.flywaydb.core.internal.database.db2.DB2MigrationMediumTest.*;

/**
 * Test to demonstrate the migration functionality using DB2.
 */
@Category(DbCategory.DB2.class)
@RunWith(Parameterized.class)
public class DB2ConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_DB2_111},
                {JDBC_URL_DB2_105}
        });
    }

    public DB2ConcurrentMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD, null);
    }
}