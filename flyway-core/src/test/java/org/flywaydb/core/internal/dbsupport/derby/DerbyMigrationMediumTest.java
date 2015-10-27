/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.derby;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Derby.
 */
@Category(DbCategory.Derby.class)
public class DerbyMigrationMediumTest extends MigrationTestCase {
    static {
        System.setProperty("derby.stream.error.field", "java.lang.System.err");
    }

    @Override
    public void tearDown() throws Exception {
        try {
            new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:derby:memory:flyway_db;drop=true", "", "").getConnection();
        } catch (FlywayException e) {
            //OK, expected error 08006. See http://db.apache.org/derby/docs/dev/devguide/cdevdvlpinmemdb.html
        }

        super.tearDown();
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:derby:memory:flyway_db;create=true", "", "");
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void bitdata() throws Exception {
        flyway.setLocations("migration/dbsupport/derby/sql/bitdata");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/derby/sql/trigger");
        flyway.migrate();

        // Fails if triggers aren't cleaned properly
        flyway.clean();
    }
}