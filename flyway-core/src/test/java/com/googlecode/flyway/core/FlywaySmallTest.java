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
package com.googlecode.flyway.core;

import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for the main Flyway class.
 */
public class FlywaySmallTest {
    @Test
    public void configure() {
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.jdbcTemplate.getDataSource());
        assertEquals("PUBLIC", flyway.getSchemas()[0]);
    }

    @Test
    public void configureTarget() {
        Properties properties = new Properties();
        properties.setProperty("flyway.target", "666");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals("666", flyway.getTarget().toString());
    }

    @Test
    public void configureSchemas() {
        Properties properties = new Properties();
        properties.setProperty("flyway.schemas", "  schema1,schema2, schema3 ,");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(3, flyway.getSchemas().length);
        assertEquals("schema1", flyway.getSchemas()[0]);
        assertEquals("schema2", flyway.getSchemas()[1]);
        assertEquals("schema3", flyway.getSchemas()[2]);
    }

    @Test
    public void configureWithExistingDataSource() {
        DataSource dataSource = new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "");

        Properties properties = new Properties();

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.configure(properties);

        assertEquals(dataSource, flyway.jdbcTemplate.getDataSource());
    }

    @Test
    public void configureWithPartialDbConfigInProperties() {
        DataSource dataSource = new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "");

        Properties properties = new Properties();
        properties.setProperty("flyway.url", "dummy_url");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.configure(properties);

        assertEquals(dataSource, flyway.jdbcTemplate.getDataSource());
    }
}
