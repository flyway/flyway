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
package org.flywaydb.core.internal.dbsupport.postgresql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLMigrationMediumTest.*;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@Category(DbCategory.PostgreSQL.class)
@RunWith(Parameterized.class)
public class PostgreSQLConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_POSTGRESQL_100},
                {JDBC_URL_POSTGRESQL_96},
                {JDBC_URL_POSTGRESQL_95},
                {JDBC_URL_POSTGRESQL_94},
                {JDBC_URL_POSTGRESQL_93},
                {JDBC_URL_POSTGRESQL_92}
        });
    }

    public PostgreSQLConcurrentMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }
}