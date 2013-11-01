/*
 * Copyright 2013 SKullal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.dbsupport.nonstop;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SKullal
 */
public class NonStopSchemaTest {

    private Flyway flyway;
    private Connection connection;
    private Properties properties;

    public NonStopSchemaTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        if (flyway == null) {
            properties = new Properties();
            properties.setProperty("flyway.user", "dba.manager");
            properties.setProperty("flyway.password", "Today006!");
            properties.setProperty("flyway.url", "jdbc:t4sqlmx://10.221.221.161:18650/:serverDataSource=PRISMDS;catalog=flywayTestCatalog;schema=schema1");
            properties.setProperty("flyway.driver", "com.tandem.t4jdbc.SQLMXDriver");
            
            flyway = new Flyway();
            flyway.configure(properties);
        }
    }

    @After
    public void tearDown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException sqlEx) {
                System.out.println(sqlEx.getMessage());
            }
        }
    }

    /**
     * Test of doExists method, of class NonStopSchema.
     */
    @Test
    public void testDoExists() throws Exception {
        connection = JdbcUtils.openConnection(flyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        assertTrue(currentSchema.exists());
    }

    /**
     * Test of doEmpty method, of class NonStopSchema.
     */
    @Test
    public void testDoEmpty() throws Exception {
        connection = JdbcUtils.openConnection(flyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        assertFalse(currentSchema.empty());
    }

    /**
     * Test of doCreate method, of class NonStopSchema.
     */
    @Test
    public void testDoCreate() throws Exception {
        Properties newProperties = new Properties(properties);
        newProperties.setProperty("flyway.url", "jdbc:t4sqlmx://10.221.221.161:18650/:serverDataSource=PRISMDS;catalog=flywayTestCatalog;schema=schema3");
        Flyway newFlyway = new Flyway();
        newFlyway.configure(newProperties);
        connection = JdbcUtils.openConnection(newFlyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        assertEquals("SCHEMA3", currentSchema.getName());
        currentSchema.create();
    }

    /**
     * Test of doDrop method, of class NonStopSchema.
     */
    @Test
    public void testDoDrop() throws Exception {
        Properties newProperties = new Properties(properties);
        newProperties.setProperty("flyway.url", "jdbc:t4sqlmx://10.221.221.161:18650/:serverDataSource=PRISMDS;catalog=flywayTestCatalog;schema=schema3");
        Flyway newFlyway = new Flyway();
        newFlyway.configure(newProperties);
        connection = JdbcUtils.openConnection(newFlyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        assertEquals("SCHEMA3", currentSchema.getName());
        currentSchema.drop();
    }

    /**
     * Test of doClean method, of class NonStopSchema.
     */
    @Test
    public void testDoClean() throws Exception {
        Properties newProperties = new Properties(properties);
        newProperties.setProperty("flyway.url", "jdbc:t4sqlmx://10.221.221.161:18650/:serverDataSource=PRISMDS;catalog=flywayTestCatalog;schema=schema2");
        Flyway newFlyway = new Flyway();
        newFlyway.configure(newProperties);
        connection = JdbcUtils.openConnection(newFlyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        assertEquals("SCHEMA2", currentSchema.getName());
        currentSchema.clean();
    }

    /**
     * Test of doAllTables method, of class NonStopSchema.
     */
    @Test
    public void testDoAllTables() throws Exception {
        connection = JdbcUtils.openConnection(flyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        currentSchema.allTables();
    }

    /**
     * Test of getTable method, of class NonStopSchema.
     */
    @Test
    public void testGetTable() {
        connection = JdbcUtils.openConnection(flyway.getDataSource());
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection);
        Schema currentSchema = dbSupport.getCurrentSchema();
        assertNotNull(currentSchema.getTable("SCHEMA_VERSION"));
    }
}