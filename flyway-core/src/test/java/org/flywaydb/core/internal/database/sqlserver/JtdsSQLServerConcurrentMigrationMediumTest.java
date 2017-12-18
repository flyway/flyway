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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;

import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.*;

/**
 * Test to demonstrate the migration functionality using SQL Server with the Jtds driver.
 */
@Category(DbCategory.SQLServer.class)
public class JtdsSQLServerConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                "jdbc:jtds:sqlserver://localhost:" + JDBC_PORT + "/flyway_db_jtds_concurrent", JDBC_USER, JDBC_PASSWORD);
    }
}
