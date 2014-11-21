/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.monetdb;

import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using MySQL.
 */
@Category(DbCategory.MonetDB.class)
public class MonetDBConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("monetdb.user", Consts.monetdbUser);
        String password = customProperties.getProperty("monetdb.password", Consts.monetdbPassword);
        String url = customProperties.getProperty("monetdb.url", Consts.monetdbUrl);

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    /* (non-Javadoc)
     * @see org.flywaydb.core.migration.ConcurrentMigrationTestCase#createFlyway()
     */
    @Override
    protected Flyway createFlyway() throws SQLException {
    	Flyway flyway = super.createFlyway();
    	flyway.setCallbacks(new MonetDBConcurrentLockManager());
    	return flyway;
    }
    
}