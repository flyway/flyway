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

import java.util.Properties;

import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.logging.StringLogCreator;
import org.junit.Test;
import static org.junit.Assert.*;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationInfo;

/**
 * Test for the main MongoFlyway class.
 */
public class MongoFlywayMediumTest  extends EmbeddedMongoDb {

	private MongoFlyway build() {
        Properties props = new Properties();
        props.setProperty("flyway.locations", "db.migrations.mongo");
        props.setProperty("flyway.validateOnMigrate", "false");
        props.setProperty("flyway.mongoUri", getMongoUri());

		MongoFlyway flyway = new MongoFlyway();
		flyway.configure(props);
		// Set mongo client so that flyway does not close it.
		flyway.setMongoClient(getMongoClient());
		return flyway;
	}

	private void createTestDb() {
        getMongoClient().getDatabase("mongoFlywayTest").createCollection("demo");
    }

	@Test
	public void baseline() {
        MongoFlyway flyway = build();
        flyway.setBaselineOnMigrate(true);
        createTestDb();
        try {
            flyway.baseline();
        } catch (FlywayException e) {
            fail("Mongo baseline failed:" + e.getLocalizedMessage());
            return;
        }
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
	}

    @Test
    public void schema() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/empty");
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            fail("Mongo schema creation failed:" + e.getLocalizedMessage());
            return;
        }
        assertEquals(MigrationType.SCHEMA, flyway.info().current().getType());
    }

	@Test
	public void migrate() {
        MongoFlyway flyway = build();
        try {
            flyway.baseline();
            flyway.migrate();
        } catch (FlywayException e) {
            fail("Mongo baseline failed:" + e.getLocalizedMessage());
            return;
        }

        MigrationInfo current = flyway.info().current();
        assertEquals(MigrationVersion.fromVersion("1.1"), current.getVersion());
        assertEquals(MigrationType.MONGODB, current.getType());
        assertEquals(MigrationState.SUCCESS, current.getState());
	}

	@Test(expected = FlywayException.class)
	public void validateNotApplied() {
        MongoFlyway flyway = build();
        flyway.validate();
	}

	@Test
	public void validateApplied() {
        MongoFlyway flyway = build();
        try {
            flyway.baseline();
            flyway.migrate();
            flyway.validate();
        } catch (FlywayException e) {
            fail("Validation failed: " + e.getLocalizedMessage());
        }

        assertTrue(true);
	}

    @Test
    public void info() throws Exception {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        assertEquals(4, flyway.info().all().length); // including schema creation migration
        assertEquals(3, flyway.info().pending().length);

        flyway.setTarget(MigrationVersion.fromVersion("1.1"));
        assertEquals(4, flyway.info().all().length); // including schema creation migration
        assertEquals(1, flyway.info().pending().length);
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[2].getState());
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[3].getState());

        flyway.migrate();
        assertEquals(-103043102, flyway.info().current().getChecksum().intValue());
        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertEquals(4, flyway.info().all().length); // including schema creation migration
        assertEquals(0, flyway.info().pending().length);

        flyway.setTarget(MigrationVersion.LATEST);
        assertEquals(4, flyway.info().all().length); // including schema creation migration
        assertEquals(2, flyway.info().pending().length);

        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertEquals(4, flyway.info().all().length); // including schema creation migration
        assertEquals(0, flyway.info().pending().length);
    }

    @Test
    public void infoPending() throws Exception {
        MongoFlyway flyway = build();
        assertEquals(1, flyway.info().pending().length);
    }

    @Test
    public void repairFirst() throws Exception {
        MongoFlyway flyway = build();

        flyway.setLocations("migration/failed");
        assertEquals(2, flyway.info().all().length);

        try {
            flyway.migrate();
        } catch (FlywayException e) {
            //Should happen
        }
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.FAILED, flyway.info().current().getState());

        flyway.repair();
        assertEquals(MigrationType.SCHEMA, flyway.info().current().getType());
    }

    @Test
    public void infoBaseline() throws Exception {
        MongoFlyway flyway = build();
        flyway.setBaselineOnMigrate(true);
        createTestDb();
        flyway.setLocations();
        flyway.baseline();

        assertEquals(1, flyway.info().all().length);
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.BASELINE, flyway.info().current().getState());
    }

    @Test
    public void baselineAgainWithSameVersion() throws Exception {
        MongoFlyway flyway = build();
        flyway.setBaselineOnMigrate(true);
        createTestDb();
        flyway.setLocations("migration/mongoscript");
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
        MongoFlyway flyway = build();
        flyway.setBaselineOnMigrate(true);
        createTestDb();
        flyway.baseline();

        flyway.setBaselineVersionAsString("2");
        flyway.baseline();
    }

    @Test(expected = FlywayException.class)
    public void cleanDisabled() throws Exception {
        MongoFlyway flyway = build();
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
        MongoFlyway flyway = build();
        flyway.setLocations("migration/validate");
        flyway.migrate();

        flyway.setCleanOnValidationError(true);
        flyway.setValidateOnMigrate(true);
        flyway.setMongoMigrationPrefix("CheckValidate");
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void baselineAfterInit() throws Exception {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/validate");
        flyway.setBaselineOnMigrate(true);
        createTestDb();
        flyway.baseline();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());

        flyway.baseline();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void baselineOnMigrate() throws Exception {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setBaselineVersionAsString("3");
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.MONGOSCRIPT, flyway.info().current().getType());

        flyway.setTable("other_metadata");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

        assertEquals("3", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void baselineRepair() throws Exception {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
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
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setBaselineVersionAsString("3");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.MONGOSCRIPT, flyway.info().current().getType());
    }

    @Test
    public void baselineOnMigrateSkipFailed() throws Exception {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setBaselineVersionAsString("3");
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.MONGOSCRIPT, flyway.info().current().getType());

        flyway.setTable("other_metadata");
        flyway.setLocations("migration/failed");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

        assertEquals("3", flyway.info().current().getVersion().toString());
        assertEquals(MigrationType.BASELINE, flyway.info().current().getType());
    }

    @Test
    public void outOfOrder() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        assertEquals(4, flyway.info().all().length); // including schema creation migration
        assertEquals(2, flyway.info().pending().length);

        flyway.clean();
        assertEquals(2, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);

        flyway.setLocations("migration/mongoscript", "migration/outoforder");
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
        MongoFlyway flyway = build();
        flyway.setTargetAsString("1.2");
        flyway.setLocations("migration/mongoscript", "migration/repeatable");
        assertEquals(4, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);

        MigrationInfo[] all = flyway.info().all();
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.SUCCESS, all[2].getState());
        assertEquals(MigrationState.SUCCESS, all[3].getState());
        assertEquals(MigrationState.SUCCESS, all[4].getState());
        assertEquals(MigrationState.ABOVE_TARGET, all[5].getState());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.setLocations("migration/mongoscript", "migration/repeatable2");
        all = flyway.info().all();
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.SUCCESS, all[2].getState());
        assertEquals(MigrationState.OUTDATED, all[3].getState());
        assertEquals(MigrationState.OUTDATED, all[4].getState());
        assertEquals(MigrationState.PENDING, all[5].getState());
        assertEquals(MigrationState.PENDING, all[6].getState());
        assertEquals(MigrationState.PENDING, all[7].getState());
        assertNotNull(all[0].getVersion());
        assertNotNull(all[1].getVersion());
        assertNotNull(all[2].getVersion());
        assertNull(all[3].getVersion());
        assertNull(all[4].getVersion());
        assertNotNull(all[5].getVersion());
        assertNull(all[6].getVersion());
        assertNull(all[7].getVersion());

        assertEquals(3, flyway.info().pending().length);

        assertEquals(3, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);
        all = flyway.info().all();
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.SUCCESS, all[2].getState());
        assertEquals(MigrationState.SUPERSEEDED, all[3].getState());
        assertEquals(MigrationState.SUPERSEEDED, all[4].getState());
        assertEquals(MigrationState.SUCCESS, all[5].getState());
        assertEquals(MigrationState.SUCCESS, all[6].getState());
        assertEquals(MigrationState.SUCCESS, all[7].getState());
        assertNotNull(all[0].getVersion());
        assertNotNull(all[1].getVersion());
        assertNotNull(all[2].getVersion());
        assertNull(all[3].getVersion());
        assertNull(all[4].getVersion());
        assertNotNull(all[5].getVersion());
        assertNull(all[6].getVersion());
        assertNull(all[7].getVersion());

        assertEquals(0, flyway.migrate());
    }

    @Test
    public void currentEmpty() {
        MongoFlyway flyway = build();
        flyway.setTargetAsString("current");
        assertEquals(0, flyway.migrate());
        // Used to fail with NPE
    }

    @Test
    public void emptyLocations() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/empty");
        assertEquals(0, flyway.migrate());
        // Used to fail with exception due to non-empty schema and empty metadata table.
        assertEquals(0, flyway.migrate());
    }

    @Test
    public void futureMigrations() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.migrate();

        flyway.setLocations("migration/empty");
        flyway.setValidateOnMigrate(true);
        flyway.migrate();
        assertEquals(MigrationState.FUTURE_SUCCESS, flyway.info().applied()[1].getState());
    }

    @Test
    public void futureMigrationsNotAllowed() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.migrate();

        flyway.setLocations("migration/empty");
        flyway.setIgnoreFutureMigrations(false);
        assertEquals(0, flyway.migrate());
    }

    @Test
    public void validateMissing() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript", "migration/outoforder");
        flyway.migrate();
        flyway.setLocations("migration/mongoscript");
        assertEquals(0, flyway.migrate());
    }

    @Test
    public void noLocations() {
        MongoFlyway flyway = build();
        flyway.setLocations();
        flyway.migrate();
    }

    @Test
    public void invalidLocations() {
        MongoFlyway flyway = build();
        flyway.setLocations("abcd", "efgh");
        flyway.migrate();
    }

    @Test
    public void validateOutOfOrder() {
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.migrate();
        flyway.validate();
        flyway.setLocations("migration/mongoscript", "migration/outoforder");
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
        MongoFlyway flyway = build();
        flyway.setLocations("migration/empty");
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateWithPendingWithoutTarget() {
        // Populate database up to version 1.2
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();

        // Validate migrations with pending migration 2.0 on classpath
        flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.validate();
    }

    @Test
    public void validateWithPendingWithTarget() {
        // Populate database up to version 1.2
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();

        // Validate migrations with pending migration 2.0 on classpath
        flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setTarget(MigrationVersion.CURRENT);
        flyway.validate();
    }

    @Test
    public void migrateWithTargetCurrent() {
        // Populate database up to version 1.2
        MongoFlyway flyway = build();
        flyway.setLocations("migration/mongoscript");
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();

        assertEquals(4, flyway.info().all().length);
        assertEquals(3, flyway.info().applied().length);
        assertEquals(0, flyway.info().pending().length);
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[3].getState());

        // This should be a no-op as target=current will ignore future migrations
        flyway = build();
        flyway.setLocations("migration/mongoscript");
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
            MongoFlyway flyway = build();
            flyway.setLocations("migration/failed");
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            System.out.println(logCreator.getOutput());
        } finally {
            LogFactory.setLogCreator(null);
        }
    }
}
