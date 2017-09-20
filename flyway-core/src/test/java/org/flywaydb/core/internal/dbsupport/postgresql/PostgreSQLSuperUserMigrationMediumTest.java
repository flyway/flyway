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
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

import static org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLMigrationMediumTest.JDBC_URL_POSTGRESQL_92;
import static org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLMigrationMediumTest.JDBC_URL_POSTGRESQL_93;

/**
 * PostgreSQL medium tests that require SuperUser permissions.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.PostgreSQL.class)
@RunWith(Parameterized.class)
public class PostgreSQLSuperUserMigrationMediumTest {
    private Flyway flyway;

    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_POSTGRESQL_93}
                // [pro]
                , {JDBC_URL_POSTGRESQL_92}
                // [/pro]
        });
    }

    public PostgreSQLSuperUserMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, "postgres", "postgres");
    }

    @Before
    public void setUp() throws Exception {
        flyway = new Flyway();
        flyway.setSchemas("super_user_test");
        flyway.setDataSource(createDataSource());
        flyway.setValidateOnMigrate(true);
        flyway.clean();
    }

    @Test
    public void setRole() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/setrole", "migration/sql");
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Types.
     */
    @Test
    public void basetype() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/basetype");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

        // Clean again, to prevent tests with non superuser rights to fail.
        flyway.clean();
    }
}
