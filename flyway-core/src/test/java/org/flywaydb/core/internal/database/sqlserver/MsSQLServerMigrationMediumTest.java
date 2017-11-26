/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using SQL Server with the Microsoft driver.
 */
@Category(DbCategory.SQLServer.class)
public class MsSQLServerMigrationMediumTest extends SQLServerMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                "jdbc:sqlserver://localhost:" + JDBC_PORT + ";databaseName=flyway_db_ms", JDBC_USER, JDBC_PASSWORD);
    }

    @Ignore("No solution for this so far as it must be run outside of a transaction with no other transaction active in the system")
    @Test
    public void singleUser() throws Exception {
        flyway.setLocations("migration/database/sqlserver/sql/singleUser");
        flyway.migrate();
    }

    @Test
    public void backup() throws Exception {
        flyway.setLocations("migration/database/sqlserver/sql/backup");
        assertEquals(1, flyway.migrate());
    }
}
