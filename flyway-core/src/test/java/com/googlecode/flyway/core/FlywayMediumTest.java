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

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Medium tests for the main Flyway class.
 */
@SuppressWarnings({"JavaDoc"})
public class FlywayMediumTest {
    @Test
    public void multipleSetDataSourceCalls() throws Exception {
        DriverDataSource dataSource1 =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_1;DB_CLOSE_DELAY=-1", "sa", "");

        DriverDataSource dataSource2 =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_2;DB_CLOSE_DELAY=-1", "sa", "");

        Connection connection1 = dataSource1.getConnection();
        Connection connection2 = dataSource2.getConnection();

        assertTrue(new H2DbSupport(connection1).isSchemaEmpty("PUBLIC"));
        assertTrue(new H2DbSupport(connection2).isSchemaEmpty("PUBLIC"));

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource1);
        assertNull(flyway.status());

        flyway.setDataSource(dataSource2);
        assertNull(flyway.status());

        flyway.setLocations("migration/sql");
        flyway.migrate();

        assertTrue(new H2DbSupport(connection1).isSchemaEmpty("PUBLIC"));
        assertFalse(new H2DbSupport(connection2).isSchemaEmpty("PUBLIC"));

        connection1.close();
        connection2.close();
    }

    @Test
    public void info() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_info;DB_CLOSE_DELAY=-1", "sa", null);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        assertNull(flyway.info().current());
        assertEquals(0, flyway.info().all().length);
        assertEquals(0, flyway.info().pending().length);

        flyway.setLocations("migration/sql");
        assertEquals(4, flyway.info().all().length);
        assertEquals(4, flyway.info().pending().length);

        flyway.setTarget(new MigrationVersion("1.1"));
        assertEquals(4, flyway.info().all().length);
        assertEquals(2, flyway.info().pending().length);
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[2].getState());
        assertEquals(MigrationState.ABOVE_TARGET, flyway.info().all()[3].getState());

        flyway.migrate();
        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertEquals(4, flyway.info().all().length);
        assertEquals(0, flyway.info().pending().length);

        flyway.setTarget(MigrationVersion.LATEST);
        assertEquals(4, flyway.info().all().length);
        assertEquals(2, flyway.info().pending().length);

        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertEquals(4, flyway.info().all().length);
        assertEquals(0, flyway.info().pending().length);
    }

    @Test
    public void repairFirst() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_repair;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        assertNull(flyway.info().current());
        assertEquals(0, flyway.info().all().length);

        flyway.setLocations("migration/failed");
        assertEquals(1, flyway.info().all().length);

        try {
            flyway.migrate();
        } catch (FlywayException e) {
            //Should happen
        }
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.FAILED, flyway.info().current().getState());

        flyway.repair();
        assertNull(flyway.info().current());
    }

    @Test
    public void infoInit() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_repair;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.init();

        assertEquals(1, flyway.info().all().length);
        assertEquals("0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
    }

    @Test
    public void outOfOrder() {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_out_of_order;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.setTarget(new MigrationVersion("1.2"));
        assertEquals(4, flyway.info().all().length);
        assertEquals(3, flyway.info().pending().length);

        flyway.clean();
        assertEquals(3, flyway.migrate());
        assertEquals(0, flyway.info().pending().length);

        flyway.setLocations("migration/sql", "migration/outoforder");
        assertEquals(5, flyway.info().all().length);
        assertEquals(MigrationState.IGNORED, flyway.info().all()[2].getState());
        assertEquals(0, flyway.migrate());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.setOutOfOrder(true);
        assertEquals(MigrationState.PENDING, flyway.info().all()[2].getState());
        assertEquals(2, flyway.migrate());

        assertEquals(MigrationState.OUT_OF_ORDER, flyway.info().all()[2].getState());
        assertEquals(MigrationState.SUCCESS, flyway.info().all()[4].getState());
    }

    @Test
    public void noConnectionLeak() {
        OpenConnectionCountDriverDataSource dataSource = createDataSource();

        assertEquals(0, dataSource.getOpenConnectionCount());
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
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
        flyway.setLocations("migration/failed");
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
        return new OpenConnectionCountDriverDataSource("jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "");
    }

    private static class OpenConnectionCountDriverDataSource extends DriverDataSource {
        /**
         * The number of connections currently open.
         */
        private int openConnectionCount = 0;

        public OpenConnectionCountDriverDataSource(String url, String user, String password) throws FlywayException {
            super(null, url, user, password);
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
