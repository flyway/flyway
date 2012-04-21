/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.h2.Driver;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Medium tests for the main Flyway class.
 */
@SuppressWarnings({"JavaDoc"})
public class FlywayMediumTest {
    @Test
    public void multipleSetDataSourceCalls() throws Exception {
        DriverDataSource dataSource1 =
                new DriverDataSource(new Driver(), "jdbc:h2:mem:flyway_db_1;DB_CLOSE_DELAY=-1", "sa", "");

        DriverDataSource dataSource2 =
                new DriverDataSource(new Driver(), "jdbc:h2:mem:flyway_db_2;DB_CLOSE_DELAY=-1", "sa", "");

        Connection connection1 = dataSource1.getConnection();
        Connection connection2 = dataSource2.getConnection();

        assertTrue(new H2DbSupport(connection1).isSchemaEmpty("PUBLIC"));
        assertTrue(new H2DbSupport(connection2).isSchemaEmpty("PUBLIC"));

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource1);
        assertNull(flyway.status());

        flyway.setDataSource(dataSource2);
        assertNull(flyway.status());

        flyway.setBaseDir("migration/sql");
        flyway.migrate();

        assertTrue(new H2DbSupport(connection1).isSchemaEmpty("PUBLIC"));
        assertFalse(new H2DbSupport(connection2).isSchemaEmpty("PUBLIC"));

        connection1.close();
        connection2.close();
    }

    @Test
    public void noConnectionLeak() {
        OpenConnectionCountDriverDataSource dataSource = createDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setBaseDir("migration/sql");
        flyway.clean();
        assertEquals(0, dataSource.getOpenConnectionCount());
        assertEquals(4, flyway.migrate());
        assertEquals(0, dataSource.getOpenConnectionCount());
    }

    @Test
    public void noConnectionLeakWithException() {
        OpenConnectionCountDriverDataSource dataSource = createDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setBaseDir("migration/failed");
        try {
            flyway.clean();
            assertEquals(0, dataSource.getOpenConnectionCount());
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected -> Ignore
        }
        assertEquals(0, dataSource.getOpenConnectionCount());
    }

    /**
     * Tests the functionality of the OpenConnectionCountDriverDataSource.
     */
    @Test
    public void connectionCount() throws Exception {
        OpenConnectionCountDriverDataSource dataSource = createDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Connection connection = dataSource.getConnection();
        assertEquals(1, dataSource.getOpenConnectionCount());
        connection.close();
        assertEquals(0, dataSource.getOpenConnectionCount());

        Connection connection2 = dataSource.getConnection();
        assertEquals(1, dataSource.getOpenConnectionCount());
        Connection connection3 = dataSource.getConnection();
        assertEquals(2, dataSource.getOpenConnectionCount());
        connection2.close();
        assertEquals(1, dataSource.getOpenConnectionCount());
        connection3.close();
        assertEquals(0, dataSource.getOpenConnectionCount());
    }

    /**
     * @return A new OpenConnectionCountDriverDataSource for the tests.
     */
    private OpenConnectionCountDriverDataSource createDataSource() {
        return new OpenConnectionCountDriverDataSource(new Driver(), "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "");
    }

    private static class OpenConnectionCountDriverDataSource extends DriverDataSource {
        /**
         * The number of connections currently open.
         */
        private int openConnectionCount = 0;

        public OpenConnectionCountDriverDataSource(Driver driver, String url, String user, String password) throws FlywayException {
            super(driver, url, user, password);
        }

        /**
         * @return The number of connections currently open.
         */
        public int getOpenConnectionCount() {
            return openConnectionCount;
        }

        @Override
        protected Connection getConnectionFromDriver(String username, String password) throws SQLException {
            final Connection connection = super.getConnectionFromDriver(username, password);

            openConnectionCount++;

            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("close".equals(method.getName())) {
                        openConnectionCount--;
                    }
                    return method.invoke(connection, args);
                }
            });
        }
    }
}
