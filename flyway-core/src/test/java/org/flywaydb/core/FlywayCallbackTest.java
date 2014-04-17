/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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

import static org.junit.Assert.*;

import java.util.Properties;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.junit.Test;

/**
 * Tests for Flyway Callbacks
 * 
 * @author Dan Bunker
 *
 */
public class FlywayCallbackTest {
    @Test
    public void propertyInstantiationTest() {
        Properties properties = createProperties();
        properties.setProperty("flyway.callbacks", "org.flywaydb.core.FlywayCallbackImpl");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.getDataSource());

        flyway.clean();
    }

    @Test
    public void cleanTest() {
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());

        flyway.clean();
        
        assertEquals(true, callbackImpl.isBeforeClean());
        assertEquals(true, callbackImpl.isAfterClean());

        //make sure no other lifecycle events were fired
        assertEquals(false, callbackImpl.isAfterEachMigrate());
        assertEquals(false, callbackImpl.isAfterInfo());
        assertEquals(false, callbackImpl.isAfterInit());
        assertEquals(false, callbackImpl.isAfterMigrate());
        assertEquals(false, callbackImpl.isAfterRepair());
        assertEquals(false, callbackImpl.isAfterValidate());

        assertEquals(false, callbackImpl.isBeforeEachMigrate());
        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(false, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isBeforeValidate());
    }

    @Test
    public void infoTest() {
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isAfterInfo());

        flyway.info();
        
        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());
        assertEquals(false, callbackImpl.isAfterEachMigrate());
        assertEquals(true, callbackImpl.isAfterInfo());
        assertEquals(false, callbackImpl.isAfterInit());
        assertEquals(false, callbackImpl.isAfterMigrate());
        assertEquals(false, callbackImpl.isAfterRepair());
        assertEquals(false, callbackImpl.isAfterValidate());

        assertEquals(false, callbackImpl.isBeforeEachMigrate());
        assertEquals(true, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(false, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isBeforeValidate());
    }

    @Test
    public void initTest() {
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(false, callbackImpl.isAfterInit());

        flyway.init();
        
        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());
        assertEquals(false, callbackImpl.isAfterEachMigrate());
        assertEquals(false, callbackImpl.isAfterInfo());
        assertEquals(true, callbackImpl.isAfterInit());
        assertEquals(false, callbackImpl.isAfterMigrate());
        assertEquals(false, callbackImpl.isAfterRepair());
        assertEquals(false, callbackImpl.isAfterValidate());

        assertEquals(false, callbackImpl.isBeforeEachMigrate());
        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(true, callbackImpl.isBeforeInit());
        assertEquals(false, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isBeforeValidate());
    }

    @Test
    public void migrateTest() {
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isAfterMigrate());

        flyway.migrate();
        
        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());
        assertEquals(false, callbackImpl.isAfterEachMigrate());
        assertEquals(false, callbackImpl.isAfterInfo());
        assertEquals(false, callbackImpl.isAfterInit());
        assertEquals(true, callbackImpl.isAfterMigrate());
        assertEquals(false, callbackImpl.isAfterRepair());
        assertEquals(false, callbackImpl.isAfterValidate());

        assertEquals(false, callbackImpl.isBeforeEachMigrate());
        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(true, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isBeforeValidate());
    }

    @Test
    public void repairTest() {
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isAfterRepair());

        flyway.repair();
        
        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());
        assertEquals(false, callbackImpl.isAfterEachMigrate());
        assertEquals(false, callbackImpl.isAfterInfo());
        assertEquals(false, callbackImpl.isAfterInit());
        assertEquals(false, callbackImpl.isAfterMigrate());
        assertEquals(true, callbackImpl.isAfterRepair());
        assertEquals(false, callbackImpl.isAfterValidate());

        assertEquals(false, callbackImpl.isBeforeEachMigrate());
        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(false, callbackImpl.isBeforeMigrate());
        assertEquals(true, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isBeforeValidate());
    }

    @Test
    public void validateTest() {
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isAfterRepair());

        flyway.validate();
        
        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());
        assertEquals(false, callbackImpl.isAfterEachMigrate());
        assertEquals(false, callbackImpl.isAfterInfo());
        assertEquals(false, callbackImpl.isAfterInit());
        assertEquals(false, callbackImpl.isAfterMigrate());
        assertEquals(false, callbackImpl.isAfterRepair());
        assertEquals(true, callbackImpl.isAfterValidate());

        assertEquals(false, callbackImpl.isBeforeEachMigrate());
        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(false, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(true, callbackImpl.isBeforeValidate());
    }

    @Test
    public void migrateEachTest() {
    	cleanTest();
        Properties properties = createProperties();

        FlywayCallbackImpl callbackImpl = new FlywayCallbackImpl();
        FlywayCallback[] callbacks = new FlywayCallback[] { callbackImpl };
        
        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callbacks);

        assertNotNull(flyway.getDataSource());

        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isAfterRepair());

        flyway.migrate();
        
        assertEquals(false, callbackImpl.isBeforeClean());
        assertEquals(false, callbackImpl.isAfterClean());
        assertEquals(true, callbackImpl.isAfterEachMigrate());
        assertEquals(false, callbackImpl.isAfterInfo());
        assertEquals(false, callbackImpl.isAfterInit());
        assertEquals(true, callbackImpl.isAfterMigrate());
        assertEquals(false, callbackImpl.isAfterRepair());
        assertEquals(false, callbackImpl.isAfterValidate());

        assertEquals(true, callbackImpl.isBeforeEachMigrate());
        assertEquals(false, callbackImpl.isBeforeInfo());
        assertEquals(false, callbackImpl.isBeforeInit());
        assertEquals(true, callbackImpl.isBeforeMigrate());
        assertEquals(false, callbackImpl.isBeforeRepair());
        assertEquals(false, callbackImpl.isBeforeValidate());
    }

    private Properties createProperties() {
		Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");
        properties.setProperty("flyway.locations", "migration/dbsupport/h2/sql/domain");
		return properties;
	}
}
