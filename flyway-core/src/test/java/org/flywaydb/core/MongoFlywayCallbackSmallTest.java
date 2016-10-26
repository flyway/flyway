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

import com.mongodb.MongoClient;
import org.flywaydb.core.api.callback.BaseMongoFlywayCallback;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests for Mongo Flyway Callbacks
 */
public class MongoFlywayCallbackSmallTest extends EmbeddedMongoDb {

    private static final String BASEDIR = "migration/subdir/dir1";

    protected MongoFlyway flyway;
    private MongoFlywayCallbackImpl callbackImpl;
    private MongoFlywayCallback[] callbacks;

    @Before
    public void setup() throws Exception {
        Properties mongoProperties = new Properties();
        mongoProperties.setProperty("flyway.locations", BASEDIR);
        mongoProperties.setProperty("flyway.validateOnMigrate", "false");
        mongoProperties.setProperty("flyway.mongoUri", getMongoUri());
        callbackImpl = new MongoFlywayCallbackImpl(flyway);
        callbacks = new MongoFlywayCallback[]{callbackImpl};
        flyway = new MongoFlyway();
        flyway.configure(mongoProperties);
        flyway.setMongoCallbacks(callbacks);
    }

    @Test
    public void cleanTest() {
        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());

        flyway.clean();

        assertTrue(callbackImpl.isBeforeClean());
        assertTrue(callbackImpl.isAfterClean());

        //make sure no other lifecycle events were fired
        assertFalse(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void infoTest() {
        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isAfterInfo());

        flyway.info();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertFalse(callbackImpl.isAfterEachMigrate());
        assertTrue(callbackImpl.isAfterInfo());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertTrue(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void baselineTest() {
        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isAfterBaseline());

        flyway.baseline();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertFalse(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertTrue(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertTrue(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void migrateTest() {
        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isAfterMigrate());

        flyway.migrate();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertTrue(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertFalse(callbackImpl.isAfterBaseline());
        assertTrue(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertTrue(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertTrue(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void repairTest() {
        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isAfterRepair());

        flyway.repair();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertFalse(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertTrue(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertTrue(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void validateTest() {
        flyway.setMongoClient(getMongoClient());
        flyway.migrate();
        callbackImpl = new MongoFlywayCallbackImpl(flyway);
        callbacks = new MongoFlywayCallback[]{callbackImpl};
        flyway.setMongoCallbacks(callbacks);

        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeValidate());
        assertFalse(callbackImpl.isAfterValidate());

        flyway.validate();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertFalse(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertTrue(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertTrue(callbackImpl.isBeforeValidate());
    }

    @Test
    public void migrateEachTest() throws Exception {
        cleanTest();
        setup();

        assertNotNull(flyway.getMongoClient());

        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isAfterRepair());

        flyway.migrate();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertTrue(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertFalse(callbackImpl.isAfterBaseline());
        assertTrue(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertTrue(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertTrue(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test(expected = IllegalStateException.class)
    public void failingCallbackTest() {
        MongoFlywayCallback failingCallback = new BaseMongoFlywayCallback(flyway) {
            @Override
            public void beforeMigrate(MongoClient client) {
                throw new IllegalStateException("Failing");
            }
        };

        flyway.setMongoCallbacks(failingCallback);

        assertNotNull(flyway.getMongoClient());
        flyway.migrate();
    }

}
