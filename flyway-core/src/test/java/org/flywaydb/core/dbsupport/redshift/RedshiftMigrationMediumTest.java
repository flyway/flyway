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
package org.flywaydb.core.dbsupport.redshift;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.util.jdbc.DriverDataSource;
import org.flywaydb.core.util.jdbc.JdbcUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test to demonstrate the concurrent migration functionality using Redshift.
 */
@Category(DbCategory.Redshift.class)
public class RedshiftMigrationMediumTest extends MigrationTestCase
{
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception
    {
        String user = customProperties.getProperty("redshift.user", "flyway");
        String password = customProperties.getProperty("redshift.password", "flyway");
        String url = customProperties.getProperty("redshift.url");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    @Override
    protected String getQuoteLocation()
    {
        return "migration/quote";
    }

    /**
     * Tests clean and migrate for Redshift Views.
     */
    @Test
    public void view() throws Exception
    {
        flyway.setLocations("migration/dbsupport/postgresql/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM \"\"\"v\"\"\""));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests parsing support for $$ string literals.
     */
    @Test
    public void dollarQuote() throws Exception
    {
        flyway.setLocations("migration/dbsupport/redshift/sql/dollar");
        flyway.migrate();
        assertEquals(9, jdbcTemplate.queryForInt("select count(*) from dollar"));
    }

    /**
     * Tests parsing support for multiline string literals.
     */
    @Test
    public void multiLine() throws Exception
    {
        flyway.setLocations("migration/dbsupport/postgresql/sql/multiline");
        flyway.migrate();
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from address"));
    }

    /**
     * Tests that the lock on SCHEMA_VERSION is not blocking SQL commands in migrations. This test won't fail if there's
     * a too restrictive lock - it would just hang endlessly.
     */
    @Test
    @Ignore
    public void lock()
    {
        flyway.setLocations("migration/dbsupport/postgresql/sql/lock");
        flyway.migrate();
    }

    @Test
    public void emptySearchPath()
    {
        Flyway flyway1 = new Flyway();
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        flyway1.setDataSource(new DriverDataSource(Thread.currentThread().getContextClassLoader(),
                                                   null, driverDataSource.getUrl(), driverDataSource.getUser(), driverDataSource.getPassword())
        {
            @Override
            public Connection getConnection() throws SQLException
            {
                Connection connection = super.getConnection();
                Statement statement = null;
                try {
                    statement = connection.createStatement();
                    statement.execute("SELECT set_config('search_path', '', false)");
                } finally {
                    JdbcUtils.closeStatement(statement);
                }
                return connection;
            }
        });
        flyway1.setLocations(BASEDIR);
        flyway1.setSchemas("public");
        flyway1.migrate();
    }
}
