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
package org.flywaydb.core.internal.dbsupport.sqlite;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using SQLite.
 */
@Category(DbCategory.SQLite.class)
public class SQLiteMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        SQLiteConfig config = new SQLiteConfig();
        config.setOpenMode(SQLiteOpenMode.CREATE);
        config.setOpenMode(SQLiteOpenMode.DELETEONCLOSE);
        config.setOpenMode(SQLiteOpenMode.READWRITE);
        config.setOpenMode(SQLiteOpenMode.TRANSIENT_DB);

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(),
                null,
                "jdbc:sqlite:target/sqlite-" + testName.getMethodName(),
                "",
                "",
                config.toProperties());
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

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE TABLE \"schema_version\" (\n" +
                "    \"version_rank\" INT NOT NULL,\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50) NOT NULL PRIMARY KEY,\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now')),\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_vr_idx\" ON \"schema_version\" (\"version_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_ir_idx\" ON \"schema_version\" (\"installed_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_s_idx\" ON \"schema_version\" (\"success\")");
    }

    @Test
    public void cleanWithSystemTables() throws Exception {
        flyway.clean();
        // AUTOINCREMENT field causes sqlite_sequence table creation
        flyway.setLocations("migration/dbsupport/sqlite/autoincrement");
        flyway.migrate();
        // crashes on "DROP sqlite_sequence": table sqlite_sequence may not be dropped
        flyway.clean();
    }

    @Test
    public void singleConnectionExternalDataSource() {
        SQLiteConfig config = new SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        SQLiteDataSource dataSource = new SQLiteDataSource(config);
        dataSource.setUrl("jdbc:sqlite:target/single_external");
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void singleConnectionInternalDataSource() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:sqlite:target/single_internal", "", "");
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.clean();
        flyway.migrate();
    }
}
