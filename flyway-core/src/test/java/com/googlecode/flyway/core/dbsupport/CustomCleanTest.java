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

import com.googlecode.flyway.core.dbsupport.sqlserver.SQLServerDbSupport;
import com.googlecode.flyway.core.dbsupport.sqlserver.SQLServerSchema;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomCleanTest {
    private static boolean customCleanCalled;

    @Before
    public void setup(){
        customCleanCalled = false;
    }

    @Test
    public void testCustomClean() throws SQLException {
        // Can't use mocks here since the factory will instantiate by class name.
        DbSupportFactory.registerCustomDbSupport("SQLServer", CustomSqlServerDbSupport.class.getName());
        Connection conn = mockConnection("Microsoft SQL Server 2008");
        DbSupport dbSupport = DbSupportFactory.createDbSupport(conn);
        assert(dbSupport instanceof CustomSqlServerDbSupport); // Sanity check
        assertFalse(customCleanCalled); // Sanity check
        dbSupport.getSchema("dbo").doClean();
        assertTrue(customCleanCalled);
    }

    // Just for testing.
    public static class CustomSqlServerDbSupport extends SQLServerDbSupport {
        public CustomSqlServerDbSupport(Connection connection) {
            super(connection);
        }

        @Override
        public Schema getSchema(String name) {
            return new CustomSqlServerSchema(super.getJdbcTemplate(), this, name);
        }
    }

    // Just for testing.
    public static class CustomSqlServerSchema extends SQLServerSchema {
        public CustomSqlServerSchema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
            super(jdbcTemplate, dbSupport, name);
        }

        @Override
        protected void doClean() throws SQLException {
            customCleanCalled = true;
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
