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
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@Category(DbCategory.SapHana.class)
public class SapHanaMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("saphana.user", "DEV_XXXXXXXXXXXXXXXXXXXXXXXXX");
        String password = customProperties.getProperty("saphana.password", "XXXXXXXXXXXXXXXXXXXX");
        String url = customProperties.getProperty("saphana.url", "jdbc:sap://localhost:30XXX");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    protected String getBasedir() {
        return "migration/dbsupport/saphana/sql/base";
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    protected String getFutureFailedLocation() {
        return "migration/dbsupport/saphana/sql/future_failed";
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/saphana/sql/sequence");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Sequence", flyway.info().current().getDescription());

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/saphana/sql/trigger");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Trigger", flyway.info().current().getDescription());

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void index() throws Exception {
        flyway.setLocations("migration/dbsupport/saphana/sql/index");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Index", flyway.info().current().getDescription());

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void statistics() throws Exception {
        flyway.setLocations("migration/dbsupport/saphana/sql/statistics");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Statistics", flyway.info().current().getDescription());

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void synonym() throws Exception {
        flyway.setLocations("migration/dbsupport/saphana/sql/synonym");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Synonym", flyway.info().current().getDescription());

        flyway.clean();
        flyway.migrate();
    }

    @Test
    @Ignore("Ignored as SAP HANA demo lacks create schema permissions")
    public void migrateMultipleSchemas() throws Exception {
    }

    @Test
    @Ignore("Ignored as SAP HANA demo lacks create schema permissions")
    public void setCurrentSchema() throws Exception {
    }

    @Test
    @Ignore("Ignored due SAP HANA syntax compatibility issues")
    public void subDir() {
    }

    @Test
    @Ignore("Ignored due SAP HANA syntax compatibility issues")
    public void outOfOrderMultipleRankIncrease() {
    }

    @Ignore("Not needed as SAP HANA support was first introduced in Flyway 4.0")
    @Override
    public void upgradeMetadataTableTo40Format() throws Exception {
    }
}