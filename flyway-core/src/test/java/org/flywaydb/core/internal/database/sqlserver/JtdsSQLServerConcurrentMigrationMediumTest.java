/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;

import javax.sql.DataSource;
import java.util.Properties;

import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.JDBC_PASSWORD;
import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.JDBC_PORT;
import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.JDBC_USER;

/**
 * Test to demonstrate the migration functionality using SQL Server with the Jtds driver.
 */
@Category(DbCategory.SQLServer.class)
public class JtdsSQLServerConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                "jdbc:jtds:sqlserver://localhost:" + JDBC_PORT + "/flyway_db_jtds_concurrent", JDBC_USER, JDBC_PASSWORD);
    }
}
