/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.nuodb;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using Oracle.
 */
@Category(DbCategory.NuoDB.class)
public class NuoDBConcurrentMigrationTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("nuodb.user", "flyway");
        String password = customProperties.getProperty("nuodb.password", "flyway");
        String url = customProperties.getProperty("nuodb.url", "jdbc:com.nuodb://localhost/flyway");
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }
}