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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.migration.ConcurrentMigrationTestCase;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
public class PostgreSQLConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("postgresql.user", "flyway");
        String password = customProperties.getProperty("postgresql.password", "flyway");
        String url = customProperties.getProperty("postgresql.url", "jdbc:postgresql://localhost/flyway_db");

        return new DriverDataSource(null, url, user, password);
    }
}