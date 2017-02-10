/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.informix;

import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;

import javax.sql.DataSource;
import java.util.Properties;
import org.flywaydb.core.api.FlywayException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test to demonstrate the migration functionality using Informix.
 */
@Category(DbCategory.Informix.class)
public class InformixMigrationMediumTest extends MigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {

        String user = customProperties.getProperty("informix.user", "informix");
        String password = customProperties.getProperty("informix.password", "in4mix");
        String url = customProperties.getProperty("informix.url", "jdbc:informix-sqli://localhost:9088/test:informixserver=dev");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Ignore("Table quote is not supported in Informix.")
    @Override
    public void quotesAroundTableName() {
    }

    @Test
    public void qQuote() throws FlywayException {
        flyway.setLocations("migration/dbsupport/informix/sql/qquote");
        flyway.migrate();
    }
}
