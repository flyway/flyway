/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.sqlite;

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
        config.enforceForeignKeys(true);

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
        flyway.setLocations("migration/database/sqlite/sql/trigger");
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

    @Test
    public void cleanWithSystemTables() throws Exception {
        flyway.clean();
        // AUTOINCREMENT field causes sqlite_sequence table creation
        flyway.setLocations("migration/database/sqlite/sql/autoincrement");
        flyway.migrate();
        // crashes on "DROP sqlite_sequence": table sqlite_sequence may not be dropped
        flyway.clean();
    }

    @Test
    public void foreignKey() throws Exception {
        flyway.clean();
        flyway.setLocations("migration/database/sqlite/sql/foreign_key");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
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
