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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;

import static org.flywaydb.core.internal.database.redshift.RedshiftMigrationMediumTest.*;
import static org.junit.Assume.assumeTrue;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@Category(DbCategory.Redshift.class)
public class RedshiftConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected void ensureTestEnabled() {
        assumeTrue(Boolean.valueOf(System.getProperty("flyway.test.redshift")));
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}