/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.mssql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using MSSQL.
 */
@SuppressWarnings({"JavaDoc"})
@ContextConfiguration(locations = {"classpath:migration/mssql/mssql-context.xml"})
public class MSSQLMigrationMediumTest extends MigrationTestCase {
    @Override
    protected String getBaseDir() {
        return "migration/sql";
    }

    @Override
    protected String getQuoteBaseDir() {
        return "migration/quote";
    }

    @Override
    protected DbSupport getDbSupport(JdbcTemplate jdbcTemplate) {
        return new MSSQLDbSupport(jdbcTemplate);
    }

    /**
     * Tests clean and migrate for MSSQL Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setBaseDir("migration/mssql/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForObject("SELECT value FROM test_data", String.class));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for MSSQL Triggers.
     */
    @Ignore
    @Test
    public void trigger() throws Exception {
        flyway.setBaseDir("migration/mssql/sql/trigger");
        flyway.migrate();

        assertEquals(10, jdbcTemplate.queryForInt("SELECT count(*) FROM test4"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for MSSQL Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setBaseDir("migration/mssql/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    @Ignore
    public void semicolonWithinStringLiteral() {
        //Ignore
    }
}
