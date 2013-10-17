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
package com.googlecode.flyway.core.dbsupport;


import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DbSupportFactoryTest {
    @Before
    public void setup() {
        DbSupportFactory.clearCustomDbSupport();
    }

    @Test
    public void createDbSupport() throws SQLException {
        Connection conn = mockConnection("H2");
        assert (DbSupportFactory.createDbSupport(conn) instanceof H2DbSupport);
    }

    @Test
    public void createDbSupportForUnsupportedDatabase() throws SQLException {
        Connection conn = mockConnection("Unsupported");
        try{
            DbSupportFactory.createDbSupport(conn);
            fail("Should fail for unsupported database.");
        }catch(FlywayException e){
            assertEquals("Unsupported Database: Unsupported 1.0", e.getMessage());
        }
    }

    @Test
    public void registerCustomDbSupport() {
        DbSupportFactory.registerCustomDbSupport("H2", CustomH2DbSupport.class.getName());
        assertEquals(CustomH2DbSupport.class, DbSupportFactory.getCustomDbSupport(H2DbSupport.class));
    }

    @Test
    public void registerCustomDbSupportForUnsupportedDatabase() {
        try{
            DbSupportFactory.registerCustomDbSupport("Unsupported", CustomH2DbSupport.class.getName());
            fail("Should fail for unsupported database.");
        }catch(FlywayException e){
            assertEquals("Unsupported Database: Unsupported", e.getMessage());
        }
    }

    @Test
    public void cannotFindCustomDbSupportClass() {
        try {
            DbSupportFactory.registerCustomDbSupport("H2", "no.such.Class");
        } catch (FlywayException e) {
            assertEquals("Could not find class: no.such.Class", e.getMessage());
            assertEquals(ClassNotFoundException.class, e.getCause().getClass());
        }
    }

    @Test
    public void clearAllCustomDbSupport() {
        DbSupportFactory.registerCustomDbSupport("H2", CustomH2DbSupport.class.getName());
        assertEquals(CustomH2DbSupport.class, DbSupportFactory.getCustomDbSupport(H2DbSupport.class));
        DbSupportFactory.clearCustomDbSupport();
        assertNull(DbSupportFactory.getCustomDbSupport(H2DbSupport.class));
    }

    @Test
    public void createCustomDbSupport() throws SQLException {
        DbSupportFactory.registerCustomDbSupport("H2", CustomH2DbSupport.class.getName());
        Connection conn = mockConnection("H2");
        assert (DbSupportFactory.createDbSupport(conn) instanceof CustomH2DbSupport);
    }

    @Test
    public void cannotInstantiateCustomDbSupport() {
        try {
            DbSupportFactory.registerCustomDbSupport("H2", NonInstantiableCustomH2DbSupport.class.getName());
        } catch (FlywayException e) {
            assertEquals("Unable to create instance of: " + NonInstantiableCustomH2DbSupport.class.getName(), e.getMessage());
        }
    }

    // Just for testing.
    public static class CustomH2DbSupport extends H2DbSupport {
        public CustomH2DbSupport(Connection conn) {
            super(conn);
        }
    }

    // Just for testing.
    public static class NonInstantiableCustomH2DbSupport extends H2DbSupport {
        public NonInstantiableCustomH2DbSupport(Connection conn) {
            super(conn);
            throw new RuntimeException("Boom");
        }
    }

    private Connection mockConnection(String productName) throws SQLException {
        Connection conn = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(conn.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn(productName);
        when(metaData.getDatabaseMajorVersion()).thenReturn(1);
        when(metaData.getDatabaseMinorVersion()).thenReturn(0);
        return conn;
    }

}
