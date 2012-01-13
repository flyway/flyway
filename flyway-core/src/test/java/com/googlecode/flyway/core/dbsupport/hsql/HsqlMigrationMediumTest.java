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
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Hsql.
 */
@ContextConfiguration(locations = {"classpath:migration/dbsupport/hsql/hsql-context.xml"})
public class HsqlMigrationMediumTest extends MigrationTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        try {
            jdbcTemplate.execute("DROP SCHEMA flyway_1 CASCADE");
            jdbcTemplate.execute("DROP SCHEMA flyway_2 CASCADE");
            jdbcTemplate.execute("DROP SCHEMA flyway_3 CASCADE");
        } catch (SQLException e) {
            //Dirty hack to compensate for the fact that DROP SCHEMA IF EXISTS is only available as of HsqlDB 2.0
        }

        jdbcTemplate.execute("CREATE SCHEMA flyway_1 AUTHORIZATION DBA");
        jdbcTemplate.execute("CREATE SCHEMA flyway_2 AUTHORIZATION DBA");
        jdbcTemplate.execute("CREATE SCHEMA flyway_3 AUTHORIZATION DBA");
    }

    @Override
    protected String getQuoteBaseDir() {
        return "migration/quote";
    }

    @Test
    public void sequence() throws Exception {
        flyway.setBaseDir("migration/dbsupport/hsql/sql/sequence");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Sequence", flyway.status().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("CALL NEXT VALUE FOR the_beast"));

        flyway.clean();
        flyway.migrate();
    }
}