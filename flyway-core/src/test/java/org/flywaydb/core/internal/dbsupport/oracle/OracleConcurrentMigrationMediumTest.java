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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.flywaydb.core.internal.dbsupport.oracle.OracleMigrationMediumTest.*;

/**
 * Test to demonstrate the migration functionality using Oracle.
 */
@Category(DbCategory.Oracle.class)
@RunWith(Parameterized.class)
public class OracleConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_ORACLE_12}



        });
    }

    private final String jdbcUrl;

    public OracleConcurrentMigrationMediumTest(String jdbcUrl) throws Exception {
        this.jdbcUrl = jdbcUrl;
        ensureOracleIsUp(createDataSource(null));
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD, null);
    }
}