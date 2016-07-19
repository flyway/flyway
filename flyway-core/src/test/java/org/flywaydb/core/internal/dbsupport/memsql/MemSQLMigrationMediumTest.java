/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.memsql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using MemSQL.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.MemSQL.class)
public class MemSQLMigrationMediumTest extends MemSQLMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("memsql.user", "root");
        String password = customProperties.getProperty("memsql.password", "");
        String url = customProperties.getProperty("memsql.url", "jdbc:mysql://127.0.0.1:43306/flyway_memsql_db");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    @Test
    public void migrateWithNonExistingSchemaSetInPropertyButNotInUrl() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:mysql://127.0.0.1:43306/", "root", "");
        flyway.setSchemas("nonexistingschema");
        flyway.setLocations(BASEDIR);
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }

    @Test
    public void migrateWithExistingSchemaSetInPropertyButNotInUrl() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:mysql://127.0.0.1:43306/", "root", "");
        flyway.setSchemas("flyway_memsql_db");
        flyway.setLocations(getBasedir());
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }
}
