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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

/**
 * PostgreSQL medium tests that require SuperUser permissions.
 */
@ContextConfiguration(locations = {"classpath:migration/dbsupport/postgresql/postgresql-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PostgreSQLSuperUserMigrationMediumTest {
    /**
     * The datasource to use for single-threaded migration tests.
     */
    @Autowired
    @Qualifier("migrationDataSourceSuperUser")
    protected DataSource migrationDataSource;

    protected JdbcTemplate jdbcTemplate;

    protected Flyway flyway;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(migrationDataSource);

        flyway = new Flyway();
        flyway.setDataSource(migrationDataSource);
        flyway.setValidationMode(ValidationMode.ALL);
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Types.
     */
    @Test
    public void type() throws Exception {
        flyway.setBaseDir("migration/dbsupport/postgresql/sql/type");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

        // Clean again, to prevent tests with non superuser rights to fail.
        flyway.clean();
    }
}
