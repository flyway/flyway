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
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * PostgreSQL medium tests that require SuperUser permissions.
 */
@SuppressWarnings({"JavaDoc"})
public class PostgreSQLSuperUserMigrationMediumTest {
    private Flyway flyway;

    @Before
    public void setUp() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }

        String password = customProperties.getProperty("postgresql.password", "flyway");
        String url = customProperties.getProperty("postgresql.url", "jdbc:postgresql://localhost/flyway_db");

        flyway = new Flyway();
        flyway.setDataSource(new DriverDataSource(null, url, "postgres", password));
        flyway.setValidationMode(ValidationMode.ALL);
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Types.
     */
    @Test
    public void type() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/type");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

        // Clean again, to prevent tests with non superuser rights to fail.
        flyway.clean();
    }
}
