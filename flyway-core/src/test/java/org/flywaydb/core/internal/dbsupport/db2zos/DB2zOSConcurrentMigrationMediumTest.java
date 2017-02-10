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
package org.flywaydb.core.internal.dbsupport.db2zos;

import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

@Category(DbCategory.DB2zOS.class)
public class DB2zOSConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("db2.user", "AURINTS");
        String password = customProperties.getProperty("db2.password", "password");
        String url = customProperties.getProperty("db2.url", "jdbc:db2://host:port/schemaname");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }
}
