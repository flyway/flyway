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
package org.flywaydb.core;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.h2.H2DbSupport;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.logging.StringLogCreator;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Medium tests for the main Flyway class.
 */
@SuppressWarnings({"JavaDoc"})
public class FlywayMediumTest {
    @Test
    public void multipleSetDataSourceCalls() throws Exception {
        DriverDataSource dataSource1 =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_1;DB_CLOSE_DELAY=-1", "sa", "");

        DriverDataSource dataSource2 =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_2;DB_CLOSE_DELAY=-1", "sa", "");

        Connection connection1 = dataSource1.getConnection();
        Connection connection2 = dataSource2.getConnection();

        Schema schema1 = new H2DbSupport(connection1).getSchema("PUBLIC");
        Schema schema2 = new H2DbSupport(connection2).getSchema("PUBLIC");

        assertTrue(schema1.empty());
        assertTrue(schema2.empty());

        Flyway flyway = new Flyway();

        flyway.setDataSource(dataSource1);
        flyway.setDataSource(dataSource2);

        flyway.setLocations("migration/sql");
        flyway.migrate();

        assertTrue(schema1.empty());
        assertFalse(schema2.empty());

        connection1.close();
        connection2.close();
    }

    @Test
    public void info() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_info;DB_CLOSE_DELAY=-1", "sa", null);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        flyway.setLocations("migration/sql");
        assertEquals(4, flyway.info().all().length);
        assertEquals(4, flyway.info().pending().length);

        flyway.setTarget(MigrationVersion.fromVersion("1.1"));
        assertEquals(4, flyway.info().all().length);
        assertEquals(2, flyway.info().pending().length);
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[2].getState());
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[3].getState());

        flyway.migrate();
        assertEquals(64723601, flyway.info().current().getChecksum().intValue());
        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertEquals(4, flyway.info().all().length);
        assertEquals(0, flyway.info().pending().length);

        flyway.setTarget(MigrationVersion.LATEST);
        assertEquals(4, flyway.info().all().length);
        assertEquals(2, flyway.info().pending().length);

        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertEquals(4, flyway.info().all().length);
        assertEquals(0, flyway.info().pending().length);
    }

    @Test
    public void callback() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_callback;DB_CLOSE_DELAY=-1", "sa", "", "SET AUTOCOMMIT OFF");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSqlMigrationPrefix("");
        flyway.setLocations("migration/callback");
        flyway.migrate();
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());

        assertEquals("Mr Callback", new JdbcTemplate(dataSource.getConnection(), 0).queryForString("SELECT name FROM test_user"));
    }

    @Test
    public void repairFirst() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_repair;DB_CLOSE_DELAY=-1", "sa", "", "SET AUTOCOMMIT OFF");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        flyway.setLocations("migration/failed");
        assertEquals(1, flyway.info().all().length);

        try {
            flyway.migrate();
        } catch (FlywayException e) {
            //Should happen
        }
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.FAILED, flyway.info().current().getState());

        flyway.repair();
        assertNull(flyway.info().current());
    }

    @Test
    public void infoBaseline() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_info_init;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.baseline();

        flyway.setLocations();
        assertEquals(1, flyway.info().all().length);
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.BASELINE, flyway.info().current().getState());
    }

    @Test
    public void baselineAgainWithSameVersion() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_init_same;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.setBaselineVersionAsString("0.5");
        flyway.baseline();
        flyway.baseline();

        assertEquals(1, flyway.info().applied().length);
        MigrationInfo current = flyway.info().current();
        assertEquals("0.5", current.getVersion().toString());
        assertEquals(MigrationType.BASELINE, current.getType());
        assertEquals(MigrationState.BASELINE, current.getState());
    }

    @Test(expected = FlywayException.class)
    public void baselineAgainWithDifferentVersion() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_init_different;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.baseline();

        flyway.setBaselineVersionAsString("2");
        flyway.baseline();
    }

    @Test(expected = FlywayException.class)
    public void cleanDisabled() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_clean_disabled;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        try {
            flyway.clean();
        } catch (FlywayException e) {
            fail("clean should succeed when cleanDisabled is false");
        }
        flyway.setCleanDisabled(true);
        flyway.clean();
    }

    @Test
    public void cleanOnValidate() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_clean_validate;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/validate");
        flyway.migrate();

        flyway.setCleanOnValidationError(true);
        flyway.setValidateOnMigrate(true);
        flyway.setSqlMigrationPrefix("CheckValidate");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void baselineAfterInit() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_baseline_init;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/validate");
        flyway.baseline();

        new JdbcTemplate(dataSource.getConnection(), 0).executeStatement("UPDATE \"new1\".\"schema_version\" SET \"type\"='BASELINE' WHERE \"type\"='BASELINE'");
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());

        flyway.baseline();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void baselineOnMigrate() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_baseline_migrate;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/sql");
        flyway.setBaselineVersionAsString("3");
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.SQL, flyway.info().current().getType());

        flyway.setTable("other_metadata");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

        assertEquals("3", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void baselineRepair() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_baseline_repair;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/sql");
        flyway.setBaselineVersionAsString("2");
        flyway.baseline();

        assertEquals("2", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());

        flyway.repair();

        assertEquals("2", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());

        flyway.migrate();

        assertEquals("2", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void baselineOnMigrateCheck() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_baseline_migrate_check;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/sql");
        flyway.setBaselineVersionAsString("3");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.SQL, flyway.info().current().getType());
    }

    @Test
    public void baselineOnMigrateSkipFailed() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_baseline_migrate_failed;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/sql");
        flyway.setBaselineVersionAsString("3");
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.SQL, flyway.info().current().getType());

        flyway.setTable("other_metadata");
        flyway.setLocations("migration/failed");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

        assertEquals("3", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void cleanUnknownSchema() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_clean_unknown;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.clean();
        flyway.clean();
    }

    @Test
    public void outOfOrder() {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_out_of_order;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        assertEquals(4, flyway.info().all().length);
        assertEquals(3, flyway.info().pending().length);

        flyway.clean();
        assertEquals(3, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);

        flyway.setLocations("migration/sql", "migration/outoforder");
        assertEquals(5, flyway.info().all().length);
        assertEquals(MigrationState.IGNORED, flyway.info().all()[2].getState());

        flyway.setValidateOnMigrate(false);
        assertEquals(0, flyway.migrate());

        flyway.setValidateOnMigrate(true);
        flyway.setTarget(MigrationVersion.LATEST);
        flyway.setOutOfOrder(true);
        assertEquals(MigrationState.PENDING, flyway.info().all()[3].getState());
        assertEquals(2, flyway.migrate());

        MigrationInfo[] all = flyway.info().all();
        assertEquals(MigrationState.OUT_OF_ORDER, all[3].getState());
        assertEquals(MigrationState.SUCCESS, flyway.info().all()[4].getState());
    }

    @Test
    public void repeatable() {
        DriverDataSource dataSource =
                new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_repeatable;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setTargetAsString("1.1");
        flyway.setLocations("migration/sql", "migration/repeatable");
        assertEquals(4, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);

        MigrationInfo[] all = flyway.info().all();
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.SUCCESS, all[2].getState());
        assertEquals(MigrationState.SUCCESS, all[3].getState());
        assertEquals(MigrationState.ABOVE_TARGET, all[4].getState());
        assertEquals(MigrationState.ABOVE_TARGET, all[5].getState());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.setLocations("migration/sql", "migration/repeatable2");
        all = flyway.info().all();
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.OUTDATED, all[2].getState());
        assertEquals(MigrationState.OUTDATED, all[3].getState());
        assertEquals(MigrationState.PENDING, all[4].getState());
        assertEquals(MigrationState.PENDING, all[5].getState());
        assertEquals(MigrationState.PENDING, all[6].getState());
        assertEquals(MigrationState.PENDING, all[7].getState());
        assertNotNull(all[0].getVersion());
        assertNotNull(all[1].getVersion());
        assertNull(all[2].getVersion());
        assertNull(all[3].getVersion());
        assertNotNull(all[4].getVersion());
        assertNotNull(all[5].getVersion());
        assertNull(all[6].getVersion());
        assertNull(all[7].getVersion());

        assertEquals(4, flyway.info().pending().length);

        assertEquals(4, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);
        all = flyway.info().all();
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.SUPERSEEDED, all[2].getState());
        assertEquals(MigrationState.SUPERSEEDED, all[3].getState());
        assertEquals(MigrationState.SUCCESS, all[4].getState());
        assertEquals(MigrationState.SUCCESS, all[5].getState());
        assertEquals(MigrationState.SUCCESS, all[6].getState());
        assertEquals(MigrationState.SUCCESS, all[7].getState());
        assertNotNull(all[0].getVersion());
        assertNotNull(all[1].getVersion());
        assertNull(all[2].getVersion());
        assertNull(all[3].getVersion());
        assertNotNull(all[4].getVersion());
        assertNotNull(all[5].getVersion());
        assertNull(all[6].getVersion());
        assertNull(all[7].getVersion());

        assertEquals(0, flyway.migrate());
    }

    @Test
    public void currentEmpty() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_current_empty;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setTargetAsString("current");
        assertEquals(0, flyway.migrate());
        // Used to fail with NPE
    }

    @Test
    public void emptyLocations() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_empty;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/empty");
        assertEquals(0, flyway.migrate());
        // Used to fail with exception due to non-empty schema and empty metadata table.
        assertEquals(0, flyway.migrate());
    }

    @Test
    public void noPlaceholderReplacement() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_no_placeholder_replacement;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.setPlaceholderReplacement(false);
        assertEquals(4, flyway.migrate());
    }

    @Test
    public void futureMigrations() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_future;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();

        flyway.setLocations("migration/empty");
        flyway.setValidateOnMigrate(true);
        flyway.migrate();
        assertEquals(MigrationState.FUTURE_SUCCESS, flyway.info().applied()[0].getState());
    }

    @Test(expected = FlywayException.class)
    public void futureMigrationsNotAllowed() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_future_not_allowed;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();

        flyway.setLocations("migration/empty");
        flyway.setIgnoreFutureMigrations(false);
        flyway.migrate();
    }

    @Test
    public void validateApplied() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_applied;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateMissing() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_missing;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql", "migration/outoforder");
        flyway.migrate();
        flyway.setLocations("migration/sql");
        flyway.migrate();
    }

    @Test
    public void placeholderDisabled() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_placeholder;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/placeholder");
        flyway.setPlaceholderReplacement(false);
        flyway.migrate();
    }

    @Test
    public void noLocations() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_locations;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations();
        flyway.migrate();
    }

    @Test
    public void invalidLocations() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_invalid;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("abcd", "efgh");
        flyway.migrate();
    }

    @Test
    public void validateOutOfOrder() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_outoforder;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();
        flyway.validate();
        flyway.setLocations("migration/sql", "migration/outoforder");
        try {
            flyway.validate();
            fail();
        } catch (FlywayException e) {
            assertTrue(e.getMessage().contains("not applied"));
        }
        flyway.setOutOfOrder(true);
        try {
            flyway.validate();
            fail();
        } catch (FlywayException e) {
            assertTrue(e.getMessage().contains("not applied"));
        }
        flyway.migrate();
        flyway.validate();
    }

    @Test
    public void validateEmpty() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_empty;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/empty");
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateNotApplied() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.validate();
    }
    
    @Test(expected = FlywayException.class)
    public void validateWithPendingWithoutTarget() {
    	// Populate database up to version 1.2
    	Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();
        
        // Validate migrations with pending migration 2.0 on classpath
        flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.validate();
    }
    
    @Test
    public void validateWithPendingWithTarget() {
    	// Populate database up to version 1.2
    	Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();
        
        // Validate migrations with pending migration 2.0 on classpath
        flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.setTarget(MigrationVersion.CURRENT);
        flyway.validate();    	
    }
    
    @Test
    public void migrateWithTargetCurrent() {
    	// Populate database up to version 1.2
    	Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();
        
        assertEquals(4, flyway.info().all().length);
        assertEquals(3, flyway.info().applied().length);
        assertEquals(0, flyway.info().pending().length);
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[3].getState());
        
        // This should be a no-op as target=current will ignore future migrations 
        flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_validate_pending;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.setTarget(MigrationVersion.CURRENT);
        flyway.migrate();
        
        assertEquals(4, flyway.info().all().length);
        assertEquals(3, flyway.info().applied().length);
        assertEquals(0, flyway.info().pending().length);
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[3].getState());
    }

    @Test
    public void failed() {
        StringLogCreator logCreator = new StringLogCreator();
        LogFactory.setLogCreator(logCreator);

        try {
            Flyway flyway = new Flyway();
            flyway.setDataSource("jdbc:h2:mem:flyway_failed;DB_CLOSE_DELAY=-1", "sa", "");
            flyway.setLocations("migration/failed");
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            System.out.println(logCreator.getOutput());
        } finally {
            LogFactory.setLogCreator(null);
        }
    }

    @Test
    public void noConnectionLeak() {
        OpenConnectionCountDriverDataSource dataSource = new OpenConnectionCountDriverDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.clean();
        assertEquals(0, dataSource.getOpenConnectionCount());
        assertEquals(4, flyway.migrate());
        assertEquals(0, dataSource.getOpenConnectionCount());
    }

    @Test
    public void noConnectionLeakWithException() {
        OpenConnectionCountDriverDataSource dataSource = new OpenConnectionCountDriverDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/failed");
        try {
            flyway.clean();
            assertEquals(0, dataSource.getOpenConnectionCount());
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected -> Ignore
        }
        assertEquals(0, dataSource.getOpenConnectionCount());
    }

    /**
     * Tests the functionality of the OpenConnectionCountDriverDataSource.
     */
    @Test
    public void connectionCount() throws Exception {
        OpenConnectionCountDriverDataSource dataSource = new OpenConnectionCountDriverDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Connection connection = dataSource.getConnection();
        assertEquals(1, dataSource.getOpenConnectionCount());
        connection.close();
        assertEquals(0, dataSource.getOpenConnectionCount());

        Connection connection2 = dataSource.getConnection();
        assertEquals(1, dataSource.getOpenConnectionCount());
        Connection connection3 = dataSource.getConnection();
        assertEquals(2, dataSource.getOpenConnectionCount());
        connection2.close();
        assertEquals(1, dataSource.getOpenConnectionCount());
        connection3.close();
        assertEquals(0, dataSource.getOpenConnectionCount());
    }

    private static class OpenConnectionCountDriverDataSource extends DriverDataSource {
        /**
         * The number of connections currently open.
         */
        private int openConnectionCount = 0;

        public OpenConnectionCountDriverDataSource() {
            super(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db_open_connection;DB_CLOSE_DELAY=-1", "sa", "");
        }

        /**
         * @return The number of connections currently open.
         */
        public int getOpenConnectionCount() {
            return openConnectionCount;
        }

        @Override
        protected Connection getConnectionFromDriver(String username, String password) throws SQLException {
            final Connection connection = super.getConnectionFromDriver(username, password);

            openConnectionCount++;

            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("close".equals(method.getName())) {
                        openConnectionCount--;
                    }
                    return method.invoke(connection, args);
                }
            });
        }
    }
}
