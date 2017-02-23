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
package org.flywaydb.core;

import org.flywaydb.core.api.callback.BaseFlywayCallback;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.junit.Test;

import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests for Flyway Callbacks
 */
public class FlywayCallbackSmallTest {

    @Test
    public void cleanTest() {
        Properties properties = createProperties("clean");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
        Properties properties = createProperties("info");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
        Properties properties = createProperties("baseline");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
        Properties properties = createProperties("migrate");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
        Properties properties = createProperties("repair");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
        Properties properties = createProperties("validate");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.migrate();
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
    public void migrateEachTest() {
        cleanTest();
        Properties properties = createProperties("migrate_each");

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

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
        FlywayCallback failingCallback = new BaseFlywayCallback() {
            @Override
            public void beforeMigrate(Connection connection) {
                throw new IllegalStateException("Failing");
            }
        };

        final Flyway flyway = new Flyway();
        flyway.configure(createProperties("failing"));
        flyway.setCallbacks(failingCallback);

        assertNotNull(flyway.getDataSource());
        flyway.migrate();
    }

    @Test
    public void propertyInstantiationTest() {
        Properties properties = createProperties("property");
        properties.setProperty("flyway.callbacks", "org.flywaydb.core.FlywayCallbackImpl");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.getDataSource());

        flyway.clean();
    }

    private Properties createProperties(String name) {
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test_callback_" + name + ";DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");
        properties.setProperty("flyway.locations", "migration/dbsupport/h2/sql/domain");
        properties.setProperty("flyway.validateOnMigrate", "false");
        return properties;
    }
}
