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
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@Category(DbCategory.H2.class)
public class H2ConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_concurrent;DB_CLOSE_DELAY=-1", "sa", "", null, "SET LOCK_TIMEOUT 100000");
    }

    @Override
    protected boolean needsBaseline() {
        return true;
    }
}