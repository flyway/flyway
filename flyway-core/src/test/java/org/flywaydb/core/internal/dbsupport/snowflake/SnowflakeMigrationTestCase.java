/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.snowflake;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Snowflake and its derivatives.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.Snowflake.class)
public class SnowflakeMigrationTestCase extends MigrationTestCase {
    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("snowflake.user", "default");
        String password = customProperties.getProperty("snowflake.password", "default");
        String url = customProperties.getProperty("snowflake.url", "jdbc:snowflake://XXXX.snowflakecomputing.com:443/?account=XXXX&warehouse=XXXX_WH&db=FLYWAY_TEST&role=SYSADMIN");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    /**
     * Tests clean and migrate for Snowflake Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/snowflake/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));
    }

    /**
     * Tests clean and migrate for Snowflake sequences.
     */
    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/snowflake/sql/sequence");
        flyway.migrate();

        assertEquals(169, jdbcTemplate.queryForInt("SELECT MAX(ID) FROM SEQT"));
    }

    /**
     * Tests clean and migrate for Snowflake internal stages.
     */
    @Test
    public void internalStage() throws Exception {
        flyway.setLocations("migration/dbsupport/snowflake/sql/stage");
        flyway.migrate();

        assertEquals(66, jdbcTemplate.queryForInt("SELECT SUM(VAL) FROM IMP"));
    }

    /**
     * Tests clean and migrate for Snowflake file formats.
     */
    @Test
    public void fileFormat() throws Exception {
        flyway.setLocations("migration/dbsupport/snowflake/sql/fileFormat");
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Snowflake file formats.
     */
    @Test
    public void function() throws Exception {
        flyway.setLocations("migration/dbsupport/snowflake/sql/function");
        flyway.migrate();

        assertEquals(68, jdbcTemplate.queryForInt("SELECT MULTIPLY(4, 17);"));
        assertEquals("SecondValue", jdbcTemplate.queryForString("SELECT VAL FROM TABLE(PUBLIC.GET_VALUE_FROM_ID(2))"));
        assertEquals(5040, jdbcTemplate.queryForInt("SELECT FACTORIAL(7)"));
    }

}