/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.sqlserver;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test to demonstrate the migration functionality using SQL Server.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class SQLServerMigrationTestCase extends MigrationTestCase {
    /**
     * The datasource to use for case-sensitive collatetion tests.
     */
    @Autowired
    @Qualifier("caseSensitiveDataSource")
    protected DataSource caseSensitiveDataSource;

    @Override
    protected String getQuoteBaseDir() {
        return "migration/quote";
    }

    /**
     * Tests clean and migrate for SQL Server Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setBaseDir("migration/dbsupport/sqlserver/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForObject("SELECT value FROM test_data", String.class));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server Triggers.
     */
    @Test
    public void trigger() throws Exception {
        flyway.setBaseDir("migration/dbsupport/sqlserver/sql/trigger");
        flyway.migrate();

        assertEquals(3, jdbcTemplate.queryForInt("SELECT priority FROM customers where name='MS Internet Explorer Team'"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setBaseDir("migration/dbsupport/sqlserver/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests a large migration that has been reported to hang on SqlServer 2005.
     */
    @Ignore("Axel: Fails due to nested transaction being opened in script, causing outer transaction not to receive COMMIT statement")
    @Test
    public void large() throws Exception {
        flyway.setBaseDir("migration/dbsupport/sqlserver/sql/large");
        flyway.setBasePackage("com.googlecode.flyway.core.dbsupport.sqlserver.large");
        flyway.setTarget(new SchemaVersion("3.1.0"));
        flyway.migrate();

        assertEquals("3.1.0", flyway.status().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.status().getState());
        assertTrue(jdbcTemplate.queryForInt("SELECT COUNT(*) FROM dbo.CHANGELOG") > 0);
    }

    @Test
    public void caseSensitiveCollation() throws Exception {
        flyway = new Flyway();
        flyway.setDataSource(caseSensitiveDataSource);
        flyway.setBaseDir(BASEDIR);
        flyway.clean();
        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());
        assertEquals("Add foreign key and super mega humongous padding to exceed the maximum column length in the metad...", flyway.status().getDescription());
        assertEquals(0, flyway.migrate());
        assertEquals(4, flyway.history().size());
        assertEquals(2, new JdbcTemplate(caseSensitiveDataSource).queryForInt("select count(*) from all_misters"));
    }
}
