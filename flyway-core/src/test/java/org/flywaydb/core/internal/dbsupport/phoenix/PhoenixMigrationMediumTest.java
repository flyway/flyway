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

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.*;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.*;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test to demonstrate the migration functionality using Phoenix.
 */
@Category(DbCategory.Phoenix.class)
public class PhoenixMigrationMediumTest extends MigrationTestCase {
    private static final Log LOG = LogFactory.getLog(PhoenixMigrationMediumTest.class);

    protected static HBaseTestingUtility testUtility = null;
    protected static DriverDataSource dataSource = null;

    @Override
    protected String getBaseDir() { return "migration/dbsupport/phoenix/sql/sql"; }

    @Override
    protected String getMigrationDir() { return "migration/dbsupport/phoenix/sql"; }

    @Override
    protected String getQuoteLocation() {
        return "migration/dbsupport/phoenix/sql/quote";
    }

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
    }

    @After
    @Override
    public void tearDown() throws Exception {
        // Don't close the connection after each test
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception {
        LOG.info("Shutting down mini-cluster");
        dataSource.close();
        testUtility.shutdownMiniCluster();
    }


    // Custom create table syntax
    @Test(expected = FlywayException.class)
    @Override
    public void nonEmptySchema() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        flyway.setLocations(getBaseDir());
        flyway.migrate();
    }

    // Custom create table syntax
    @Test
    @Override
    public void nonEmptySchemaWithInit() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        flyway.setLocations(getBaseDir());
        flyway.setBaselineVersionAsString("0");
        flyway.baseline();
        flyway.migrate();
    }

    // Custom create table syntax
    @Test
    @Override
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

    // Custom create table syntax
    @Test
    @Override
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

    // Phoenix has a LIKE with newline issue:
    // https://issues.apache.org/jira/browse/PHOENIX-1351
    @Test
    @Override
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setLocations("migration/dbsupport/phoenix/sql/semicolon");
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());
        assertEquals("Mr. Semicolon+Linebreak;\nanother line",
                jdbcTemplate.queryForString("SELECT name FROM test_user ORDER BY LENGTH(NAME) DESC LIMIT 1"));
    }

    // The default schema doesn't exist until something has been
    // created in the schema
    @Test
    @Override
    public void schemaExists() throws SQLException {
        assertFalse(dbSupport.getCurrentSchema().exists());
        assertFalse(dbSupport.getSchema("InVaLidScHeMa").exists());

        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL PRIMARY KEY\n" +
                "  )");

        assertTrue(dbSupport.getCurrentSchema().exists());
    }

    // Need to rename the migration prefix, otherwise unrelated classpath
    // scanner tests fail
    @Test(expected = FlywayException.class)
    @Override
    public void validateFails() throws Exception {
        flyway.setLocations(getBaseDir());
        flyway.setSqlMigrationSuffix("First.sql");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());

        flyway.setSqlMigrationPrefix("PhoenixCheckValidate");
        flyway.validate();
    }

    // Phoenix doesn't support setting an explicit schema
    @Ignore
    @Override
    public void setCurrentSchema() throws Exception {}


}