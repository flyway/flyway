/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.ingres;

import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Ingres.
 */
@SuppressWarnings({"JavaDoc"})
public class IngresMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("ingres.user", "flyway");
        String password = customProperties.getProperty("ingres.password", "flyway");
        String url = customProperties.getProperty("ingres.url", "jdbc:ingres://localhost:II7/flyway_db");

        return new DriverDataSource(null, url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    /**
     * Tests clean and migrate for Ingres Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForString("SELECT value FROM test_data"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Ingres Functions.
     */
    @Test
    public void function() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/function");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Ingres Triggers.
     */
    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/trigger");
        flyway.migrate();

        assertEquals(10, jdbcTemplate.queryForInt("SELECT count(*) FROM test4"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

    }

    /**
     * Tests clean and migrate for Ingres Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Ingres child tables.
     */
    @Test
    public void inheritance() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/inheritance");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Ingres Domains.
     */
    @Test
    public void domain() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/domain");
        flyway.migrate();

        assertEquals("foo", jdbcTemplate.queryForString("SELECT x FROM t"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Ingres Enums.
     */
    @Test
    public void enumeration() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/enum");
        flyway.migrate();

        assertEquals("positive", jdbcTemplate.queryForString("SELECT x FROM t"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Aggregates.
     */
    @Test
    public void aggregate() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/aggregate");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests parsing support for $$ string literals.
     */
    @Test
    public void dollarQuote() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/dollar");
        flyway.migrate();
        assertEquals(9, jdbcTemplate.queryForInt("select count(*) from dollar"));
    }

    /**
     * Tests parsing support for multiline string literals.
     */
    @Test
    public void multiLine() throws Exception {
        flyway.setLocations("migration/dbsupport/ingres/sql/multiline");
        flyway.migrate();
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from address"));
    }
}
