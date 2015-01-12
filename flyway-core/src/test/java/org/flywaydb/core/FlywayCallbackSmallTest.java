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
package org.flywaydb.core;

import org.flywaydb.core.api.callback.FlywayCallback;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests for Flyway Callbacks
 */
public class FlywayCallbackSmallTest {
    @Test
    public void cleanTest() {
        Properties properties = createProperties(0);

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
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void infoTest() {
        Properties properties = createProperties(1);

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
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertTrue(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void initTest() {
        Properties properties = createProperties(2);

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[]{callbackImpl};

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());

        flyway.init();

        assertFalse(callbackImpl.isBeforeClean());
        assertFalse(callbackImpl.isAfterClean());
        assertFalse(callbackImpl.isAfterEachMigrate());
        assertFalse(callbackImpl.isAfterInfo());
        assertTrue(callbackImpl.isAfterInit());
        assertTrue(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertTrue(callbackImpl.isBeforeInit());
        assertTrue(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void migrateTest() {
        Properties properties = createProperties(3);

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
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());
        assertTrue(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertTrue(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertTrue(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void repairTest() {
        Properties properties = createProperties(4);

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
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertTrue(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertTrue(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void validateTest() {
        Properties properties = createProperties(5);

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
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());
        assertFalse(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertTrue(callbackImpl.isAfterValidate());

        assertFalse(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertFalse(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertTrue(callbackImpl.isBeforeValidate());
    }

    @Test
    public void migrateEachTest() {
        cleanTest();
        Properties properties = createProperties(6);

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
        assertFalse(callbackImpl.isAfterInit());
        assertFalse(callbackImpl.isAfterBaseline());
        assertTrue(callbackImpl.isAfterMigrate());
        assertFalse(callbackImpl.isAfterRepair());
        assertFalse(callbackImpl.isAfterValidate());

        assertTrue(callbackImpl.isBeforeEachMigrate());
        assertFalse(callbackImpl.isBeforeInfo());
        assertFalse(callbackImpl.isBeforeInit());
        assertFalse(callbackImpl.isBeforeBaseline());
        assertTrue(callbackImpl.isBeforeMigrate());
        assertFalse(callbackImpl.isBeforeRepair());
        assertFalse(callbackImpl.isBeforeValidate());
    }

    @Test
    public void propertyInstantiationTest() {
        Properties properties = createProperties(7);
        properties.setProperty("flyway.callbacks", "org.flywaydb.core.FlywayCallbackImpl");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.getDataSource());

        flyway.clean();
    }

    private Properties createProperties(int num) {
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test_callback_" + num + ";DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");
        properties.setProperty("flyway.locations", "migration/dbsupport/h2/sql/domain");
        properties.setProperty("flyway.validateOnMigrate", "false");
        return properties;
    }
}
