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
package org.flywaydb.core.internal.dbsupport.sqlite;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test to demonstrate the migration functionality using SQLite.
 */
@Category(DbCategory.SQLite.class)
public class SQLiteMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:sqlite::memory:", "", "");
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Ignore
    public void migrateMultipleSchemas() throws Exception {
        //Not supported by SQLite
    }

    @Ignore
    public void setCurrentSchema() throws Exception {
        //Not supported by SQLite
    }

    @Ignore
    public void schemaExists() throws SQLException {
        //Not supported by SQLite
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlite/sql/trigger");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void noDriverCrashIssue746() throws Exception {
        flyway.setLocations(getBasedir());

        Properties props = new Properties();
        //uncomment this to fix the code
        //props.setProperty("flyway.driver", "org.sqlite.JDBC");
        props.setProperty("flyway.url", "jdbc:sqlite::memory:");
        props.setProperty("flyway.user", "sa");
        flyway.configure(props);

        //first invocation works fine
        flyway.info();
        //here comes the crash
        flyway.migrate();
    }
}
