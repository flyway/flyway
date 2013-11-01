/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
/**
 * skulal.
 *
 */
package com.googlecode.flyway.core.dbsupport.nonstop;

import com.googlecode.flyway.core.Flyway;
import org.junit.Test;

import java.util.Properties;


/**
 * Test to demonstrate the migration functionality using HP NonStop.
 */
public class NonstopMigrationMediumTest {
    
    private Flyway getFlyway(){
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "dba.manager");
        properties.setProperty("flyway.password", "Today006!");
        properties.setProperty("flyway.url", "jdbc:t4sqlmx://10.221.221.161:18650/:serverDataSource=PRISMDS;catalog=flywayTestCatalog;schema=schema1");
        properties.setProperty("flyway.driver", "com.tandem.t4jdbc.SQLMXDriver");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        return flyway;
    }

    @Test
    public void trigger() throws Exception {
        Flyway flyway = getFlyway();
        flyway.setLocations("migration/dbsupport/nonstop/sql/trigger");
        flyway.migrate();

        //flyway.clean();
        //flyway.migrate();
    }

   
}