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
package org.flywaydb.core.internal.dbsupport.saphana;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using SAP HANA.
 */
@Category(DbCategory.SapHana.class)
public class SapHanaConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("saphana.user", "DEV_XXXXXXXXXXXXXXXXXXXXXXXXX");
        String password = customProperties.getProperty("saphana.password", "XXXXXXXXXXXXXXXXXXXX");
        String url = customProperties.getProperty("saphana.url", "jdbc:sap://localhost:30XXX");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    protected String getBasedir() {
        return "migration/dbsupport/saphana/sql/base";
    }

    @Override
    protected String getSchemaName(DbSupport dbSupport) {
        return dbSupport.getCurrentSchemaName();
    }
}