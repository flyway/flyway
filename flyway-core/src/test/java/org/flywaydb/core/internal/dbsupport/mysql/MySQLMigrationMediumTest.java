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
package org.flywaydb.core.internal.dbsupport.mysql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.MySQL.class)
public class MySQLMigrationMediumTest extends MySQLMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("mysql.user", "flyway");
        String password = customProperties.getProperty("mysql.password", "flyway");
        String url = customProperties.getProperty("mysql.url", "jdbc:mysql://localhost/flyway_db");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    @Test
    public void migrateWithNonExistingSchemaSetInPropertyButNotInUrl() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:mysql://localhost/", "flyway", "flyway");
        flyway.setSchemas("non-existing-schema");
        flyway.setLocations(BASEDIR);
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }

    @Test
    public void migrateWithExistingSchemaSetInPropertyButNotInUrl() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:mysql://localhost/", "flyway", "flyway");
        flyway.setSchemas("flyway_db");
        flyway.setLocations(getBasedir());
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }
}
