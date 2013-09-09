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
package com.googlecode.flyway.core;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for the main Flyway class.
 */
public class FlywaySmallTest {
    @Test
    public void configure() {
        final Flyway flyway = getFlyway();

        assertNotNull(flyway.getDataSource());

        flyway.execute(new Flyway.Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                assertEquals("SCHEMA1", flyway.getSchemas()[0]);
                return null;
            }
        });
    }

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
    public void schemaExists() {
        Flyway flyway = getFlyway();

        flyway.execute(new Flyway.Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                //assertEquals("NRT", flyway.getSchemas()[0]);
                assert schemas[0].exists();
                assert !schemas[0].empty();
                assertNotNull(schemas[0].allTables());
                return null;
            }
        });
    }
    
    /**
     * This must be possible to enable NTLM authentication on SQL Server.
     */
    /*@Test
    public void configureNoUserNoPassword() {
        Properties properties = new Properties();
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.getDataSource());
    }*/

    /*@Test
    public void configureTarget() {
        Properties properties = new Properties();
        properties.setProperty("flyway.target", "666");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals("666", flyway.getTarget().toString());
    }*/

    /*@Test
    public void configureOutOfOrder() {
        Properties properties = new Properties();
        properties.setProperty("flyway.outOfOrder", "true");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(true, flyway.isOutOfOrder());
    }

    @Test
    public void configureValidationMode() {
        Properties properties = new Properties();
        properties.setProperty("flyway.validationMode", "ALL");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(ValidationMode.ALL, flyway.getValidationMode());
    }

    @Test
    public void configureSchemas() {
        Properties properties = new Properties();
        properties.setProperty("flyway.schemas", "  schema1,schema2, schema3 ,");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(3, flyway.getSchemas().length);
        assertEquals("schema1", flyway.getSchemas()[0]);
        assertEquals("schema2", flyway.getSchemas()[1]);
        assertEquals("schema3", flyway.getSchemas()[2]);
    }

    @Test
    public void configureWithExistingDataSource() {
        DataSource dataSource = new DriverDataSource(null, "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "");

        Properties properties = new Properties();

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.configure(properties);

        assertEquals(dataSource, flyway.getDataSource());
    }

    @Test
    public void configureWithPartialDbConfigInProperties() {
        DataSource dataSource = new DriverDataSource(null, "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "");

        Properties properties = new Properties();
        properties.setProperty("flyway.user", "dummy_user");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.configure(properties);

        assertEquals(dataSource, flyway.getDataSource());
    }

    @Test
    public void dataSourceWithSeparateParams() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "");

        assertNotNull(flyway.getDataSource());
    }

    @Test
    public void getLocations() {
        Flyway flyway = new Flyway();
        flyway.setLocations("db/migrations1", "filesystem:db/migrations2");
        String[] locations = flyway.getLocations();
        assertEquals(2, locations.length);
        assertEquals("classpath:db/migrations1", locations[0]);
        assertEquals("filesystem:db/migrations2", locations[1]);
    }*/
    
    public static void main(String[] args) {
        FlywaySmallTest test = new FlywaySmallTest();
        test.configure();
    }
}
