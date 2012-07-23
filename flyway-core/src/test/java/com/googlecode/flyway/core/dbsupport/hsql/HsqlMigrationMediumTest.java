/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.hsqldb.jdbcDriver;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Hsql.
 */
public class HsqlMigrationMediumTest extends MigrationTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        jdbcTemplate.execute("DROP SCHEMA flyway_1 IF EXISTS CASCADE");
        jdbcTemplate.execute("DROP SCHEMA flyway_2 IF EXISTS CASCADE");
        jdbcTemplate.execute("DROP SCHEMA flyway_3 IF EXISTS CASCADE");

        jdbcTemplate.execute("CREATE SCHEMA flyway_1 AUTHORIZATION DBA");
        jdbcTemplate.execute("CREATE SCHEMA flyway_2 AUTHORIZATION DBA");
        jdbcTemplate.execute("CREATE SCHEMA flyway_3 AUTHORIZATION DBA");
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(new jdbcDriver(), "jdbc:hsqldb:mem:flyway_db", "SA", "");
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/hsql/sql/sequence");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Sequence", flyway.status().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("CALL NEXT VALUE FOR the_beast"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/hsql/sql/trigger");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }
}