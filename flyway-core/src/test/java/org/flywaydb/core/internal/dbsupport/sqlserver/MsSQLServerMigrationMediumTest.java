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
package org.flywaydb.core.internal.dbsupport.sqlserver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using SQL Server with the Microsoft driver.
 */
@Category(DbCategory.SQLServer.class)
public class MsSQLServerMigrationMediumTest extends SQLServerMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("sqlserver.user", "sa");
        String password = customProperties.getProperty("sqlserver.password", "flyway");
        String url = customProperties.getProperty("sqlserver.ms_url", "jdbc:sqlserver://localhost:1433;databaseName=flyway_db_ms");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    /**
     * Tests migrate error for pk constraints.
     */
    @Ignore("Seems to be a bug in the Microsoft driver as Jtds works fine")
    @Test(expected = FlywayException.class)
    public void pkConstraints() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/pkConstraint");
        flyway.migrate();
    }

    @Ignore("No solution for this so far as it must be run outside of a transaction with no other transaction active in the system")
    @Test
    public void singleUser() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/singleUser");
        flyway.migrate();
    }
}
