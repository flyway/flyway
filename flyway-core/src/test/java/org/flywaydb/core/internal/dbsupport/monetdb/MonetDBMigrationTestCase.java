/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.monetdb;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Test to demonstrate the migration functionality using MonetDB.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class MonetDBMigrationTestCase extends MigrationTestCase {
    @Override
    protected String getQuoteLocation() {
        return "migration/dbsupport/monetdb/sql/quote";
    }

    /**
     * Tests clean and migrate for MySQL Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
    	flyway.setLocations("migration/dbsupport/monetdb/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForString("SELECT value FROM test_data"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    @Test
    public void delimiter() throws Exception {
        flyway.setLocations("migration/dbsupport/monetdb/sql/delimiter");
        flyway.migrate();
    }


    /**
     * Tests clean and migrate for MySQL Triggers.
     */
    @Ignore // problem with trigger definition
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/monetdb/sql/trigger");
        flyway.migrate();

        assertEquals(10, jdbcTemplate.queryForInt("SELECT count(*) FROM test4"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for MySQL Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/monetdb/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();
        flyway.init(); // FIXME why???

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for MySQL dumps.
     */
    @Ignore // mysql dumps are not compatible, need good test data
    public void dump() throws Exception {
        flyway.setLocations("migration/dbsupport/monetdb/sql/dump");
        flyway.migrate();

        assertEquals(0, jdbcTemplate.queryForInt("SELECT count(id) FROM user_account"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for MySQL InnoDb tables with upper case names.
     * <p/>
     * Only effective on Windows when the server is configured using:
     * <p/>
     * [mysqld]
     * lower-case-table-names=0
     * <p/>
     * This should be added to a file called my.cnf
     * <p/>
     * The server can then be started with this command:
     * <p/>
     * mysqld --defaults-file="path/to/my.cnf"
     */
    @Test
    public void upperCase() throws Exception {
        flyway.setLocations("migration/dbsupport/monetdb/sql/uppercase");
        flyway.migrate();

        assertEquals(0, jdbcTemplate.queryForInt("SELECT count(*) FROM A1"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests parsing support for " string literals.
     */
    @Test
    public void doubleQuote() throws FlywayException {
        flyway.setLocations("migration/dbsupport/monetdb/sql/doublequote");
        flyway.migrate();
    }

    /**
     * Tests parsing support for \' in string literals.
     */
    @Test
    public void escapeSingleQuote() throws FlywayException {
    	flyway.setLocations("migration/dbsupport/monetdb/sql/escape");
        flyway.setCallbacks(new MonetDBCallback());
        flyway.migrate();
    }

    /**
     * Tests whether locking problems occur when Flyway's DB connection gets reused.
     */
    @Test
    public void lockOnConnectionReUse() throws SQLException {
        DataSource twoConnectionsDataSource = new TwoConnectionsDataSource(flyway.getDataSource());
        flyway.setDataSource(twoConnectionsDataSource);
        flyway.setLocations(BASEDIR);
        flyway.migrate();

        Connection connection1 = twoConnectionsDataSource.getConnection();
        Connection connection2 = twoConnectionsDataSource.getConnection();
        assertEquals(2, new JdbcTemplate(connection1, 0).queryForInt("SELECT COUNT(*) FROM test_user"));
        assertEquals(2, new JdbcTemplate(connection2, 0).queryForInt("SELECT COUNT(*) FROM test_user"));
    }

    private static class TwoConnectionsDataSource extends AbstractDataSource {
        private final DataSource[] dataSources;
        private int count;

        public TwoConnectionsDataSource(DataSource dataSource) throws SQLException {
            dataSources = new DataSource[] {
                    new SingleConnectionDataSource(dataSource.getConnection(), true),
                    new SingleConnectionDataSource(dataSource.getConnection(), true)
            };
        }

        public Connection getConnection() throws SQLException {
            return dataSources[count++ % dataSources.length].getConnection();
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        public Logger getParentLogger() {
            throw new UnsupportedOperationException("getParentLogger");
        }
    }
}
