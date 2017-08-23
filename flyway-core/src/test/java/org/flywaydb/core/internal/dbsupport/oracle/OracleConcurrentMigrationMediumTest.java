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
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.OracleContainer;

import javax.sql.DataSource;
import java.util.Properties;

import static org.flywaydb.core.internal.dbsupport.oracle.OracleMigrationMediumTest.DOCKER_IMAGE_NAME;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleMigrationMediumTest.initOracleContainer;

/**
 * Test to demonstrate the migration functionality using Oracle.
 */
@Category(DbCategory.Oracle.class)
public class OracleConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @ClassRule
    public static OracleContainer oracle = new OracleContainer(DOCKER_IMAGE_NAME);

    @BeforeClass
    public static void beforeClass() throws Exception {
        initOracleContainer(oracle);
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                oracle.getJdbcUrl(), "flyway", "flyway", null);
    }
}