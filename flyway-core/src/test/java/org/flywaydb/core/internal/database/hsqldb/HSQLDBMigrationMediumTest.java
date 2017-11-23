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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Hsql.
 */
@Category(DbCategory.HSQL.class)
public class HSQLDBMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:hsqldb:mem:flyway_db", "SA", "", null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/database/hsqldb/sql/sequence");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Sequence", flyway.info().current().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("CALL NEXT VALUE FOR the_beast"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/database/hsqldb/sql/trigger");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void view() throws Exception {
        Schema schema = database.getMainConnection().getSchema("MY_VIEWS");
        schema.create();

        flyway.setSchemas("PUBLIC", "MY_VIEWS");
        flyway.setLocations("migration/database/hsqldb/sql/view");
        flyway.migrate();
        flyway.clean();

        schema.drop();
    }
}