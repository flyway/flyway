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
package org.flywaydb.core.internal.dbsupport.mysql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExternalResource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.HostPortWaitStrategy;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@Category(DbCategory.MariaDB.class)
public class MariaDBMigrationMediumTest extends MySQLMigrationTestCase {
    private static final String DOCKER_IMAGE_NAME = "mariadb:10.0.31";

    private static String jdbcUrl;
    private static String jdbcUser;
    private static String jdbcPassword;

    @ClassRule
    public static ExternalResource initMariaDB() {
        return new ExternalResource() {
            private MariaDBContainer mariadb;

            @Override
            protected void before() throws Throwable {
                try {
                    DockerClientFactory.instance().client();
                    mariadb = new MariaDBContainer(DOCKER_IMAGE_NAME);
                    mariadb.start();
                    new HostPortWaitStrategy().waitUntilReady(mariadb);
                    jdbcUrl = mariadb.getJdbcUrl();
                    jdbcUser = "root";
                    jdbcPassword = mariadb.getPassword();
                } catch (Exception e) {
                    // Docker not found, fall back to local MariaDB instance.
                    jdbcUrl = customProperties.getProperty("mariadb.url", "jdbc:mariadb://localhost:3333/flyway_db");
                    jdbcUser = customProperties.getProperty("mariadb.user", "flyway");
                    jdbcPassword = customProperties.getProperty("mariadb.password", "flyway");
                }
            }

            @Override
            protected void after() {
                if (mariadb != null) {
                    mariadb.stop();
                }
            }
        };
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, jdbcUser, jdbcPassword, null);
    }
}
