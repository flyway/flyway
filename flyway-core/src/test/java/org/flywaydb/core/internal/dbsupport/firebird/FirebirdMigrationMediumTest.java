/**
 * Copyright 2010-2014 Axel Fontaine
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.firebird;

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;

import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Test to demonstrate the migration functionality using Firebird.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.Firebird.class)
public class FirebirdMigrationMediumTest extends MigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("firebird.user", "flyway");
        String password = customProperties.getProperty("firebird.password", "flyway");
        String url = customProperties.getProperty("firebird.url", "jdbc:firebirdsql://localhost/flyway?encoding=UTF8");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Ignore
    @Override
    public void migrateMultipleSchemas() throws Exception {
        //schemas not supported by Firebird
    }

    @Ignore
    @Override
    public void setCurrentSchema() throws Exception {
        //schemas not supported by Firebird
    }

    @Ignore
    @Override
    public void schemaExists() throws SQLException {
        //schemas not supported by Firebird
    }

    @Ignore
    @Override
    public void quote() throws Exception {
        //schemas not supported by Firebird
    }

}
