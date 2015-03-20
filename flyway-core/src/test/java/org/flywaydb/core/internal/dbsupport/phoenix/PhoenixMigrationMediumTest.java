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
package org.flywaydb.core.internal.dbsupport.phoenix;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.*;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.*;
import org.junit.experimental.categories.Category;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

import org.apache.hadoop.hbase.HBaseTestingUtility;

/**
 * Test to demonstrate the migration functionality using Phoenix.
 */
@Category(DbCategory.Phoenix.class)
public class PhoenixMigrationMediumTest extends MigrationTestCase {
    private static final Log LOG = LogFactory.getLog(PhoenixMigrationMediumTest.class);

    @Override
    protected String getBaseDir() { return "migration/dbsupport/phoenix/sql/sql"; }

    protected static HBaseTestingUtility testUtility = null;
    protected static DriverDataSource dataSource = null;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception {
        // Startup HBase in-memory cluster
        LOG.info("Starting mini-cluster");
        testUtility = new HBaseTestingUtility();
        testUtility.startMiniCluster();

        // Set up Phoenix schema
        String server = testUtility.getConfiguration().get("hbase.zookeeper.quorum");
        String port = testUtility.getConfiguration().get("hbase.zookeeper.property.clientPort");
        String zkServer = server + ":" + port;

        dataSource = new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:phoenix:" + zkServer, "", "");
    }


    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
       return dataSource;
       //return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:phoenix:dbserver", "", "");
    }

    @After
    public void tearDown() throws Exception {
        // Don't close the connection after each test
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception {
        LOG.info("Shutting down mini-cluster");
        dataSource.close();
        testUtility.shutdownMiniCluster();
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/dbsupport/phoenix/sql/quote";
    }

    @Test
    public void repair() throws Exception {
        flyway.setLocations("migration/dbsupport/phoenix/sql/future_failed");
        assertEquals(4, flyway.info().all().length);

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        if (dbSupport.supportsDdlTransactions()) {
            assertEquals("2.0", flyway.info().current().getVersion().toString());
            assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        } else {
            assertEquals("3", flyway.info().current().getVersion().toString());
            assertEquals(MigrationState.FAILED, flyway.info().current().getState());
        }

        flyway.repair();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
    }

    @Test
    public void repairChecksum() {
        flyway.setLocations("migration/dbsupport/phoenix/sql/comment");
        Integer commentChecksum = flyway.info().pending()[0].getChecksum();

        flyway.setLocations(getQuoteLocation());
        Integer quoteChecksum = flyway.info().pending()[0].getChecksum();

        assertNotEquals(commentChecksum, quoteChecksum);

        flyway.migrate();
        assertEquals(quoteChecksum, flyway.info().applied()[1].getChecksum());

        flyway.setLocations("migration/dbsupport/phoenix/sql/comment");
        flyway.repair();
        assertEquals(commentChecksum, flyway.info().applied()[1].getChecksum());
    }

    @Test(expected = FlywayException.class)
    public void validateMoreAppliedThanAvailable() throws Exception {
        flyway.setLocations(getBaseDir());
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());

        flyway.setLocations("migration/dbsupport/phoenix/sql/validate");
        flyway.validate();
    }

    @Test
    public void validateClean() throws Exception {
        flyway.setLocations("migration/dbsupport/phoenix/sql/validate");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());

        flyway.setValidateOnMigrate(true);
        flyway.setCleanOnValidationError(true);
        flyway.setSqlMigrationPrefix("PhoenixCheckValidate");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations("migration/dbsupport/phoenix/sql/failed");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", dbSupport.quote(tableName));
        flyway.setPlaceholders(placeholders);

        try {
            flyway.migrate();
            fail();
        } catch (FlywaySqlScriptException e) {
            System.out.println(e.getMessage());
            // root cause of exception must be defined, and it should be FlywaySqlScriptException
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof SQLException);
            // and make sure the failed statement was properly recorded
            assertEquals(21, e.getLineNumber());
            assertEquals("THIS IS NOT VALID SQL", e.getStatement());
        }

        MigrationInfo migration = flyway.info().current();
        assertEquals(
                dbSupport.supportsDdlTransactions(),
                !dbSupport.getCurrentSchema().getTable(tableName).exists());
        if (dbSupport.supportsDdlTransactions()) {
            assertNull(migration);
        } else {
            MigrationVersion version = migration.getVersion();
            assertEquals("1", version.toString());
            assertEquals("Should Fail", migration.getDescription());
            assertEquals(MigrationState.FAILED, migration.getState());
            assertEquals(2, flyway.info().applied().length);
        }
    }

    @Test
    public void futureFailedMigration() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations("migration/dbsupport/phoenix/sql/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setLocations(getBaseDir());
        if (dbSupport.supportsDdlTransactions()) {
            flyway.migrate();
        } else {
            try {
                flyway.migrate();
                fail();
            } catch (FlywayException e) {
                //Expected
            }
        }
    }

    @Test
    public void futureFailedMigrationIgnore() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations("migration/dbsupport/phoenix/sql/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        flyway.setLocations(getBaseDir());
        flyway.migrate();
    }

    @Test
    public void futureFailedMigrationIgnoreAvailableMigrations() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations("migration/dbsupport/phoenix/sql/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            if (dbSupport.supportsDdlTransactions()) {
                assertTrue(e.getMessage().contains("THIS IS NOT VALID SQL"));
            } else {
                assertTrue(e.getMessage().contains("contains a failed migration"));
            }
        }
    }

    @Test(expected = FlywayException.class)
    public void nonEmptySchema() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        flyway.setLocations(getBaseDir());
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInit() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        flyway.setLocations(getBaseDir());
        flyway.setBaselineVersionAsString("0");
        flyway.baseline();
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrate() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        flyway.setLocations(getBaseDir());
        flyway.setBaselineVersionAsString("0");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.BASELINE, migrationInfos[0].getType());
        assertEquals("0", migrationInfos[0].getVersion().toString());

        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrateHighVersion() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        flyway.setLocations(getBaseDir());
        flyway.setBaselineOnMigrate(true);
        flyway.setBaselineVersion(MigrationVersion.fromVersion("99"));
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.SQL, migrationInfos[0].getType());
        assertEquals("1", migrationInfos[0].getVersion().toString());
        assertEquals(MigrationState.BELOW_BASELINE, migrationInfos[0].getState());

        MigrationInfo migrationInfo = flyway.info().current();
        assertEquals(MigrationType.BASELINE, migrationInfo.getType());
        assertEquals("99", migrationInfo.getVersion().toString());
    }

    @Test
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setLocations("migration/dbsupport/phoenix/sql/semicolon");
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());
        assertEquals("Mr. Semicolon+Linebreak;\nanother line",
                jdbcTemplate.queryForString("SELECT * FROM test_user ORDER BY LENGTH(NAME) DESC LIMIT 1"));
    }

    @Test
    public void migrateMultipleSchemas() throws Exception {
        flyway.setSchemas("flyway_1", "flyway_2", "flyway_3");
        flyway.clean();

        flyway.setLocations("migration/dbsupport/phoenix/sql/multi");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema1", dbSupport.quote("flyway_1"));
        placeholders.put("schema2", dbSupport.quote("flyway_2"));
        placeholders.put("schema3", dbSupport.quote("flyway_3"));
        flyway.setPlaceholders(placeholders);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals("Add foreign key", flyway.info().current().getDescription());
        assertEquals(0, flyway.migrate());

        assertEquals(4, flyway.info().applied().length);
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_1") + ".test_user1"));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_2") + ".test_user2"));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_3") + ".test_user3"));

        flyway.clean();
    }

    @Ignore
    public void setCurrentSchema() throws Exception {
        //Not supported by Phoenix
    }

    @Test
    public void subDir() {
        flyway.setLocations("migration/dbsupport/phoenix/sql/subdir");
        assertEquals(3, flyway.migrate());
    }

    @Test
    public void comment() {
        flyway.setLocations("migration/dbsupport/phoenix/sql/comment");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void outOfOrderMultipleRankIncrease() {
        flyway.setLocations("migration/dbsupport/phoenix/sql/sql");
        flyway.migrate();

        flyway.setLocations("migration/dbsupport/phoenix/sql/sql", "migration/dbsupport/phoenix/sql/outoforder");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        assertEquals(org.flywaydb.core.api.MigrationState.OUT_OF_ORDER, flyway.info().all()[3].getState());
    }

    @Test
    public void schemaExists() throws SQLException {
        assertFalse(dbSupport.getSchema("InVaLidScHeMa").exists());
    }
}