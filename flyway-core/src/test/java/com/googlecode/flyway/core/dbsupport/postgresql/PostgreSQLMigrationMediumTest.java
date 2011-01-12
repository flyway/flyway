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
package com.googlecode.flyway.core.dbsupport.postgresql;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.MigrationTestCase;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@SuppressWarnings({"JavaDoc"})
@ContextConfiguration(locations = {"classpath:migration/postgresql/postgresql-context.xml"})
public class PostgreSQLMigrationMediumTest extends MigrationTestCase {
    @Override
    protected String getBaseDir() {
        return "migration/sql";
    }

    @Override
    protected DbSupport getDbSupport(JdbcTemplate jdbcTemplate) {
        return new PostgreSQLDbSupport(jdbcTemplate);
    }

    /**
     * Tests clean and migrate for MySQL Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setBaseDir("migration/postgresql/sql/procedure");
        flyway.migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(migrationDataSource);
        assertEquals("Hello", jdbcTemplate.queryForObject("SELECT value FROM test_data", String.class));
        
        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Triggers.
     */
    
    @Test
    public void trigger() throws Exception {
        flyway.setBaseDir("migration/postgresql/sql/trigger");
        flyway.migrate();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(migrationDataSource);
        assertEquals(10, jdbcTemplate.queryForInt("SELECT count(*) FROM test4"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
        
    }
	
    /**
     * Tests clean and migrate for MySQL Views.
     */
    
    @Test
    public void view() throws Exception {
        flyway.setBaseDir("migration/postgresql/sql/view");
        flyway.migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(migrationDataSource);
        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }
    
}
