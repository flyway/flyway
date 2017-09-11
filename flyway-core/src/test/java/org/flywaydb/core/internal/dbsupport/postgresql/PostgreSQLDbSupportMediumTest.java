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
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLMigrationMediumTest.DOCKER_IMAGE_NAME;
import static org.junit.Assert.assertEquals;

@Category(DbCategory.PostgreSQL.class)
public class PostgreSQLDbSupportMediumTest {
    @ClassRule
    public static PostgreSQLContainer postgreSQL = new PostgreSQLContainer(DOCKER_IMAGE_NAME);

    /**
     * Checks that the search_path is extended and not overwritten so that objects in PUBLIC can still be found.
     */
    @Test
    public void setCurrentSchema() throws Exception {
        Connection connection = createDataSource().getConnection();
        PostgreSQLDbSupport dbSupport = new PostgreSQLDbSupport(connection);
        Schema schema = dbSupport.getSchema("search_path_test");
        try {
            schema.drop();
        } catch (Exception e) {
            // Ignore
        }
        schema.create();
        dbSupport.changeCurrentSchemaTo(dbSupport.getSchema("search_path_test"));
        String searchPath = dbSupport.getJdbcTemplate().queryForString("SHOW search_path");
        assertEquals("search_path_test, \"$user\", public", searchPath);
        schema.drop();
        JdbcUtils.closeConnection(connection);
    }

    /**
     * Creates a datasource for use in tests.
     *
     * @return The new datasource.
     */
    private DataSource createDataSource() throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                postgreSQL.getJdbcUrl(), postgreSQL.getUsername(), postgreSQL.getPassword(), null);
    }
}