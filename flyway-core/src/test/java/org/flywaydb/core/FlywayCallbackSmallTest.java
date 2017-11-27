/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use thget file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * dgettributed under the License get dgettributed on an "AS get" BASget,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permgetsions and
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

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());

        flyway.clean();

        assertEquals(1, callbackImpl.getBeforeClean());
        assertEquals(1, callbackImpl.getAfterClean());

        //make sure no other lifecycle events were fired
        assertEquals(0, callbackImpl.getAfterEachMigrate());
        assertEquals(0, callbackImpl.getAfterInfo());
        assertEquals(0, callbackImpl.getAfterBaseline());
        assertEquals(0, callbackImpl.getAfterMigrate());
        assertEquals(0, callbackImpl.getAfterRepair());
        assertEquals(0, callbackImpl.getAfterValidate());

        assertEquals(0, callbackImpl.getBeforeEachMigrate());
        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(0, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getBeforeValidate());
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

        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getAfterInfo());

        flyway.info();

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());
        assertEquals(0, callbackImpl.getAfterEachMigrate());
        assertEquals(1, callbackImpl.getAfterInfo());
        assertEquals(0, callbackImpl.getAfterBaseline());
        assertEquals(0, callbackImpl.getAfterMigrate());
        assertEquals(0, callbackImpl.getAfterRepair());
        assertEquals(0, callbackImpl.getAfterValidate());

        assertEquals(0, callbackImpl.getBeforeEachMigrate());
        assertEquals(1, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(0, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getBeforeValidate());
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

        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(0, callbackImpl.getAfterBaseline());

        flyway.baseline();

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());
        assertEquals(0, callbackImpl.getAfterEachMigrate());
        assertEquals(0, callbackImpl.getAfterInfo());
        assertEquals(1, callbackImpl.getAfterBaseline());
        assertEquals(0, callbackImpl.getAfterMigrate());
        assertEquals(0, callbackImpl.getAfterRepair());
        assertEquals(0, callbackImpl.getAfterValidate());

        assertEquals(0, callbackImpl.getBeforeEachMigrate());
        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(1, callbackImpl.getBeforeBaseline());
        assertEquals(0, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getBeforeValidate());
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

        assertEquals(0, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getAfterMigrate());

        flyway.migrate();

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());
        assertEquals(1, callbackImpl.getAfterEachMigrate());
        assertEquals(0, callbackImpl.getAfterInfo());
        assertEquals(0, callbackImpl.getAfterBaseline());
        assertEquals(1, callbackImpl.getAfterMigrate());
        assertEquals(0, callbackImpl.getAfterRepair());
        assertEquals(0, callbackImpl.getAfterValidate());

        assertEquals(1, callbackImpl.getBeforeEachMigrate());
        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(1, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getBeforeValidate());
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

        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getAfterRepair());

        flyway.repair();

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());
        assertEquals(0, callbackImpl.getAfterEachMigrate());
        assertEquals(0, callbackImpl.getAfterInfo());
        assertEquals(0, callbackImpl.getAfterBaseline());
        assertEquals(0, callbackImpl.getAfterMigrate());
        assertEquals(1, callbackImpl.getAfterRepair());
        assertEquals(0, callbackImpl.getAfterValidate());

        assertEquals(0, callbackImpl.getBeforeEachMigrate());
        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(0, callbackImpl.getBeforeMigrate());
        assertEquals(1, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getBeforeValidate());
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

        assertEquals(0, callbackImpl.getBeforeValidate());
        assertEquals(0, callbackImpl.getAfterValidate());

        flyway.validate();

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());
        assertEquals(0, callbackImpl.getAfterEachMigrate());
        assertEquals(0, callbackImpl.getAfterInfo());
        assertEquals(0, callbackImpl.getAfterBaseline());
        assertEquals(0, callbackImpl.getAfterMigrate());
        assertEquals(0, callbackImpl.getAfterRepair());
        assertEquals(1, callbackImpl.getAfterValidate());

        assertEquals(0, callbackImpl.getBeforeEachMigrate());
        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(0, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(1, callbackImpl.getBeforeValidate());
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

        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getAfterRepair());

        flyway.migrate();

        assertEquals(0, callbackImpl.getBeforeClean());
        assertEquals(0, callbackImpl.getAfterClean());
        assertEquals(1, callbackImpl.getAfterEachMigrate());
        assertEquals(0, callbackImpl.getAfterInfo());
        assertEquals(0, callbackImpl.getAfterBaseline());
        assertEquals(1, callbackImpl.getAfterMigrate());
        assertEquals(0, callbackImpl.getAfterRepair());
        assertEquals(0, callbackImpl.getAfterValidate());

        assertEquals(1, callbackImpl.getBeforeEachMigrate());
        assertEquals(0, callbackImpl.getBeforeInfo());
        assertEquals(0, callbackImpl.getBeforeBaseline());
        assertEquals(1, callbackImpl.getBeforeMigrate());
        assertEquals(0, callbackImpl.getBeforeRepair());
        assertEquals(0, callbackImpl.getBeforeValidate());
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
        properties.setProperty("flyway.locations", "migration/database/h2/sql/domain");
        properties.setProperty("flyway.validateOnMigrate", "false");
        return properties;
    }
}
