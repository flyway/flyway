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

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.logging.LogCreator;
import com.googlecode.flyway.core.util.logging.LogFactory;
import com.googlecode.flyway.core.util.logging.StringLogCreator;
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
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_1;DB_CLOSE_DELAY=-1", "sa", "");

        DriverDataSource dataSource2 =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_2;DB_CLOSE_DELAY=-1", "sa", "");

        Connection connection1 = dataSource1.getConnection();
        Connection connection2 = dataSource2.getConnection();

        Schema schema1 = new H2DbSupport(connection1).getSchema("PUBLIC");
        Schema schema2 = new H2DbSupport(connection2).getSchema("PUBLIC");

        assertTrue(schema1.empty());
        assertTrue(schema2.empty());

        Flyway flyway = new Flyway();

        flyway.setDataSource(dataSource1);
        flyway.setDataSource(dataSource2);

        flyway.setLocations("migration/sql");
        flyway.migrate();

        assertTrue(schema1.empty());
        assertFalse(schema2.empty());

        connection1.close();
        connection2.close();
    }

    @Test
    public void info() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_info;DB_CLOSE_DELAY=-1", "sa", null);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

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
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_info_init;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.init();

        flyway.setLocations();
        assertEquals(1, flyway.info().all().length);
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
    }

    @Test
    public void initAgainWithSameVersion() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_init_same;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("migration/sql");
        flyway.setInitVersion("0.5");
        flyway.init();
        flyway.init();

        assertEquals(1, flyway.info().applied().length);
        MigrationInfo current = flyway.info().current();
        assertEquals("0.5", current.getVersion().toString());
        assertEquals(MigrationType.INIT, current.getType());
        assertEquals(MigrationState.SUCCESS, current.getState());
    }

    @Test(expected = FlywayException.class)
    public void initAgainWithDifferentVersion() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_init_different;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.init();

        flyway.setInitVersion("2");
        flyway.init();
    }

    @Test
    public void initOnMigrateOnCleanOnValidate() throws Exception {
        DriverDataSource dataSource =
                new DriverDataSource(null, "jdbc:h2:mem:flyway_db_init_validate;DB_CLOSE_DELAY=-1", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas("new1");
        flyway.setLocations("migration/validate");
        flyway.migrate();

        flyway.setCleanOnValidationError(true);
        flyway.setValidateOnMigrate(true);
        flyway.setSqlMigrationPrefix("CheckValidate");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
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
    public void emptyLocations() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_empty;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/empty");
        assertEquals(0, flyway.migrate());
        // Used to fail with exception due to non-empty schema and empty metadata table.
        assertEquals(0, flyway.migrate());
    }

    @Test
    public void futureMigrations() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_future;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();

        flyway.setLocations("migration/empty");
        assertEquals(MigrationState.FUTURE_SUCCESS, flyway.info().applied()[0].getState());
    }

    @Test
    public void failed() {
        StringLogCreator logCreator = new StringLogCreator();
        LogFactory.setLogCreator(logCreator);

        try {
            Flyway flyway = new Flyway();
            flyway.setDataSource("jdbc:h2:mem:flyway_failed;DB_CLOSE_DELAY=-1", "sa", "");
            flyway.setLocations("migration/failed");
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            System.out.println(logCreator.getOutput());
        } finally {
            LogFactory.setLogCreator(null);
        }
    }

    @Test
    public void noConnectionLeak() {
        OpenConnectionCountDriverDataSource dataSource = new OpenConnectionCountDriverDataSource();

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
        OpenConnectionCountDriverDataSource dataSource = new OpenConnectionCountDriverDataSource();

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
        OpenConnectionCountDriverDataSource dataSource = new OpenConnectionCountDriverDataSource();

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

    private static class OpenConnectionCountDriverDataSource extends DriverDataSource {
        /**
         * The number of connections currently open.
         */
        private int openConnectionCount = 0;

        public OpenConnectionCountDriverDataSource() {
            super(null, "jdbc:h2:mem:flyway_db_open_connection;DB_CLOSE_DELAY=-1", "sa", "");
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
