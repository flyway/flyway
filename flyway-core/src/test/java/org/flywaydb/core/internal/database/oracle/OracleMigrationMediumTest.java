/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Test to demonstrate the migration functionality using Oracle.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.Oracle.class)
@RunWith(Parameterized.class)
public class OracleMigrationMediumTest extends MigrationTestCase {
    static final String JDBC_URL_ORACLE_12 = "jdbc:oracle:thin:@//localhost:62042/xe";
    static final String JDBC_URL_ORACLE_11 = "jdbc:oracle:thin:@//localhost:62041/xe";
    static final String JDBC_URL_ORACLE_10 = "jdbc:oracle:thin:@//localhost:62040/xe";
    static final String JDBC_USER = "flyway";
    static final String JDBC_PASSWORD = "flyway";

    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_ORACLE_12},
                {JDBC_URL_ORACLE_11},
                {JDBC_URL_ORACLE_10}
        });
    }

    public OracleMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD, null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    private enum OracleEdition {XE, SE, EE}

    private void assumeOracleEditionNotLessThan(OracleEdition expectedEdition) {
        OracleEdition edition;
        try {
            edition = OracleEdition.valueOf(jdbcTemplate.queryForString(
                    "SELECT CASE WHEN BANNER LIKE '%Enterprise%' THEN 'EE'" +
                            " WHEN BANNER LIKE '%Express%' THEN 'XE' ELSE 'SE' END " +
                            "FROM V$VERSION WHERE BANNER LIKE 'Oracle Database%'"));
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
        assumeTrue("Oracle edition is " + expectedEdition + " or higher", edition.compareTo(expectedEdition) >= 0);
    }

    /**
     * Tests migrations containing placeholders.
     */
    @Test
    public void migrationsWithPlaceholders() throws Exception {
        int countUserObjects1 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", "test_user");
        flyway.setPlaceholders(placeholders);
        flyway.setLocations("migration/database/oracle/sql/placeholders");

        flyway.migrate();
        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1.1", version.toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        assertEquals("Mr. T triggered", jdbcTemplate.queryForString("select name from test_user"));

        flyway.clean();

        int countUserObjects2 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");
        assertEquals(countUserObjects1, countUserObjects2);

        MigrationInfo[] migrationInfos = flyway.info().applied();
        for (MigrationInfo migrationInfo : migrationInfos) {
            assertNotNull(migrationInfo.getScript() + " has no checksum", migrationInfo.getChecksum());
        }
    }

    /**
     * Tests clean for Oracle Spatial Extensions.
     */
    @Test
    public void cleanSpatialExtensions() throws Exception {
        assertEquals(0, userObjectsCount());

        flyway.setLocations("migration/database/oracle/sql/spatial");
        flyway.migrate();
        assertTrue(userObjectsCount() > 0);

        flyway.clean();
        assertEquals(0, userObjectsCount());

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
        assertTrue(userObjectsCount() > 0);

        flyway.clean();
    }

    /**
     * Tests parsing of CREATE PACKAGE.
     */
    @Test
    public void createPackage() throws FlywayException {
        flyway.setLocations("migration/database/oracle/sql/package");
        flyway.migrate();
        flyway.clean();
    }

    @Test
    public void schemaWithDash() throws FlywayException {
        assumeDatabaseVersionNotLessThan(11, 1);
        flyway.setSchemas("my-schema");
        flyway.setLocations(getBasedir());
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    @Override
    public void migrateMultipleSchemas() throws Exception {
        assumeDatabaseVersionNotLessThan(11, 1);
        super.migrateMultipleSchemas();
    }

    @Test
    public void count() throws FlywayException {
        flyway.setLocations("migration/database/oracle/sql/count");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests parsing of object names that contain keywords such as MY_TABLE.
     */
    @Test
    public void objectNames() throws FlywayException {
        flyway.setLocations("migration/database/oracle/sql/objectnames");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests cleaning up after CREATE MATERIALIZED VIEW.
     */
    @Test
    public void createMaterializedView() throws FlywayException {
        assumeOracleEditionNotLessThan(OracleEdition.SE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/materialized");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Test clean with recycle bin
     */
    @Test
    public void cleanWithRecycleBin() throws Exception {
        assertEquals(0, userRecycleBinCount());

        // in SYSTEM tablespace the recycle bin is deactivated
        jdbcTemplate.update("CREATE TABLE test_user (name VARCHAR(25) NOT NULL,  PRIMARY KEY(name)) tablespace USERS");
        jdbcTemplate.update("DROP TABLE test_user");
        assertTrue(userRecycleBinCount() > 0);

        flyway.clean();
        assertEquals(0, userRecycleBinCount());
    }

    /**
     * @return The number of objects for the current user.
     */
    private int userObjectsCount() throws Exception {
        return jdbcTemplate.queryForInt("select count(*) from user_objects");
    }

    /**
     * @return The number of objects in the recycle bin for the current user.
     */
    private int userRecycleBinCount() throws Exception {
        return jdbcTemplate.queryForInt("select count(*) from recyclebin");
    }

    /**
     * Tests cleaning up with Scheduler objects.
     */
    @Test
    public void schedulerObjects() throws Exception {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/scheduler");
        flyway.migrate();
        assertTrue(schedJobExists("FLYWAY_AUX", "TEST_JOB"));
        flyway.clean();
        assertFalse(schedJobExists("FLYWAY_AUX", "TEST_JOB"));
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests cleaning up with Scheduler 11.2 enhancements.
     * This is also to ensure that skipping CREDENTIALs doesn't break migrations.
     */
    @Test
    public void scheduler112Enhancement() {
        assumeDatabaseVersionNotLessThan(11, 2);
        assumeOracleEditionNotLessThan(OracleEdition.SE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/scheduler11_2/create");
        flyway.migrate();
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/scheduler11_2/clean");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * @return {@code true} if the specified job exists in the schema, {@code false} if not.
     */
    @SuppressWarnings("SameParameterValue")
    private boolean schedJobExists(String schemaName, String jobName) throws Exception {
        return ((OracleDatabase) database).queryReturnsRows("SELECT * FROM ALL_SCHEDULER_JOBS " +
                "WHERE OWNER = ? AND JOB_NAME = ?", schemaName, jobName);
    }

    /**
     * Tests parsing support for q-Quote string literals.
     */
    @Test
    public void qQuote() throws FlywayException {
        flyway.setLocations("migration/database/oracle/sql/qquote");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for user defined types.
     */
    @Test
    public void type() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/type");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for create procedure.
     */
    @Test
    public void procedure() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/procedure");
        flyway.migrate();
        flyway.clean();
    }

    @Test
    public void sqlplus() throws FlywayException {
        flyway.setLocations("migration/database/oracle/sql/sqlplus");
        assertEquals(2, flyway.migrate());
    }

    /**
     * Tests support for create function.
     */
    @Test
    public void function() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/function");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for create trigger. Ensures that a Statement is used instead of a PreparedStatement.
     * <p/>
     * Reference: http://docs.oracle.com/cd/E11882_01/java.112/e16548/oraint.htm#CHDIIDBE
     * <p/>
     * Also ensures that schema-level triggers are properly cleaned.
     */
    @Test
    public void trigger() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/trigger");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for clean together with Oracle Text indexes.
     */
    @Test
    public void text() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/text");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for clean together with domain indexes, index types and operators.
     */
    @Test
    public void domainIndex() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/domain_index");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for clean together with Index Organized Tables.
     */
    @Test
    public void indexOrganizedTable() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/iot");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for clean together with Nested Tables.
     */
    @Test
    public void nestedTable() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/nested");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for clean together with queue tables.
     */
    @Test
    public void queueTable() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/queue");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for clean together with cluster tables.
     */
    @Test
    public void cluster() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/cluster");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    @Test
    public void commentOracle() throws Exception {
        flyway.setLocations("migration/database/oracle/sql/comment");
        assertEquals(3, flyway.migrate());

        String statusWithComment = jdbcTemplate.queryForString("select ob.STATUS from user_objects ob where ob.OBJECT_NAME = 'PERSON_WITH_COMMENT' ");
        String statusWithoutComment = jdbcTemplate.queryForString("select ob.STATUS from user_objects ob where ob.OBJECT_NAME = 'PERSON_WITHOUT_COMMENT' ");
        assertEquals("VALID", statusWithoutComment);
        assertEquals("VALID", statusWithComment);
        flyway.clean();
    }

    /**
     * Tests support for clean together with XML Type.
     */
    @Test
    public void xml() throws FlywayException {
        assumeDatabaseVersionNotLessThan(11, 1);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/xml");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning of tables with Flashback/Total Recall enabled.
     * Schema containing such tables has to be first cleaned by disabling flashback on each table;
     */
    @Test
    public void flashback() throws FlywayException {
        assumeDatabaseVersionNotLessThan(11, 1);
        assumeOracleEditionNotLessThan(OracleEdition.SE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/flashback");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for reference partitioned tables.
     */
    @Test
    public void referencePartitionedTable() throws FlywayException {
        assumeDatabaseVersionNotLessThan(11, 1);
        assumeOracleEditionNotLessThan(OracleEdition.EE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/refpart");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with JAVA SOURCE, JAVA CLASS, JAVA RESOURCE types.
     */
    @Test
    public void javaObjects() throws FlywayException {
        assumeOracleEditionNotLessThan(OracleEdition.SE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/java");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with CONTEXT type.
     */
    @Test
    public void context() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/context");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with LIBRARY type.
     */
    @Test
    public void library() throws FlywayException {
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/library");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with DIMENSION type.
     */
    @Test
    public void dimension() throws FlywayException {
        assumeOracleEditionNotLessThan(OracleEdition.EE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/dimension");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with MINING MODEL type.
     */
    @Test
    public void mining() throws FlywayException {
        assumeOracleEditionNotLessThan(OracleEdition.EE);
        // Starting from Oracle 11.1 Data Mining API works with non-default schemas.
        if (database.getMajorVersion() >= 11) {
            flyway.setSchemas("FLYWAY_AUX");
        }
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/mining");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with REWRITE EQUIVALENCE type.
     */
    @Test
    public void rewriteEquivalence() throws FlywayException {
        assumeOracleEditionNotLessThan(OracleEdition.SE);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/adv_rewrite");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with DATABASE LINK type.
     * This is to ensure that skipping DATABASE LINKs doesn't break migrations.
     */
    @Test
    public void dbLink() throws FlywayException {
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/dblink/create");
        flyway.migrate();
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/dblink/clean");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with RULE, RULE SET, EVALUATION CONTEXT, FILE GROUP types.
     */
    @Test
    public void streamsAndRules() throws FlywayException {
        assumeDatabaseVersionNotLessThan(11, 1);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/streams_rules");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests support for cleaning together with SQL TRANSLATION PROFILE type.
     */
    @Test
    public void sqlTranslator() throws FlywayException {
        assumeDatabaseVersionNotLessThan(12, 1);
        flyway.setSchemas("FLYWAY_AUX");
        flyway.clean();
        flyway.setLocations("migration/database/oracle/sql/sql_translator");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Checks that cleaning can not be performed for the SYSTEM schema (Issue 102)
     */
    @Test(expected = FlywayException.class)
    public void createCleanScriptWithSystem() {
        flyway.setSchemas("SYSTEM");
        flyway.clean();
    }

    /**
     * Checks that cleaning can not be performed for an Oracle-maintained schema
     */
    @Test(expected = FlywayException.class)
    public void createCleanScriptWithOracleMaintainedSchema() {
        flyway.setSchemas("OUTLN");
        flyway.clean();
    }
}
