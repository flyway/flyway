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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test to demonstrate the migration functionality using Oracle.
 */
@SuppressWarnings({"JavaDoc"})
public class OracleMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("oracle.user", "flyway");
        String password = customProperties.getProperty("oracle.password", "flyway");
        String url = customProperties.getProperty("oracle.url", "jdbc:oracle:thin:@localhost:1521:XE");

        return new DriverDataSource("oracle.jdbc.OracleDriver", url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    /**
     * Tests migrations containing placeholders.
     */
    @Test
    public void migrationsWithPlaceholders() throws Exception {
        int countUserObjects1 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", "test_user");
        flyway.setPlaceholders(placeholders);
        flyway.setLocations("migration/dbsupport/oracle/sql/placeholders");

        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1.1", schemaVersion.toString());
        assertEquals("Populate table", flyway.status().getDescription());

        assertEquals("Mr. T triggered", jdbcTemplate.queryForString("select name from test_user"));

        flyway.clean();

        int countUserObjects2 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");
        assertEquals(countUserObjects1, countUserObjects2);

        final List<MetaDataTableRow> metaDataTableRows = flyway.history();
        for (MetaDataTableRow metaDataTableRow : metaDataTableRows) {
            assertNotNull(metaDataTableRow.getScript() + " has no checksum", metaDataTableRow.getChecksum());
        }
    }

    /**
     * Tests clean for Oracle Spatial Extensions.
     */
    @Test
    public void cleanSpatialExtensions() throws Exception {
        assertEquals(0, objectsCount());

        flyway.setLocations("migration/dbsupport/oracle/sql/spatial");
        flyway.migrate();
        assertTrue(objectsCount() > 0);

        flyway.clean();
        assertEquals(0, objectsCount());

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
        assertTrue(objectsCount() > 0);
    }

    /**
     * Tests parsing of CREATE PACKAGE.
     */
    @Test
    public void createPackage() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/package");
        flyway.migrate();
    }

    /**
     * Tests parsing of object names that contain keywords such as MY_TABLE.
     */
    @Test
    public void objectNames() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/objectnames");
        flyway.migrate();
    }

    /**
     * Tests cleaning up after CREATE MATERIALIZED VIEW.
     */
    @Ignore("Disabled due to missing functionality in Oracle XE 11g. Works fine with XE 10g.")
    @Test
    public void createMaterializedView() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/materialized");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Test clean with recycle bin
     */
    @Test
    public void cleanWithRecycleBin() throws Exception {
        assertEquals(0, recycleBinCount());

        // in SYSTEM tablespace the recycle bin is deactivated
        jdbcTemplate.update("CREATE TABLE test_user (name VARCHAR(25) NOT NULL,  PRIMARY KEY(name)) tablespace USERS");
        jdbcTemplate.update("DROP TABLE test_user");
        assertTrue(recycleBinCount() > 0);

        flyway.clean();
        assertEquals(0, recycleBinCount());
    }

    /**
     * @return The number of objects for the current user.
     */
    private int objectsCount() throws Exception {
        return jdbcTemplate.queryForInt("select count(*) from user_objects");
    }

    /**
     * @return The number of objects in the recycle bin.
     */
    private int recycleBinCount() throws Exception {
        return jdbcTemplate.queryForInt("select count(*) from recyclebin");
    }

    /**
     * Tests parsing support for q-Quote string literals.
     */
    @Test
    public void qQuote() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/qquote");
        flyway.migrate();
    }

    /**
     * Tests support for user defined types.
     */
    @Test
    public void type() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/type");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for create procedure.
     */
    @Test
    public void procedure() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/procedure");
        flyway.migrate();
    }

    /**
     * Tests support for create function.
     */
    @Test
    public void function() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/function");
        flyway.migrate();
    }

    /**
     * Tests support for create trigger. Ensures that a Statement is used instead of a PreparedStatement.
     * <p/>
     * Reference: http://docs.oracle.com/cd/E11882_01/java.112/e16548/oraint.htm#CHDIIDBE
     * <p/>
     * Also ensures that schema-level triggers are properly cleaned.
     */
    @Test
    public void trigger() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/trigger");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for clean together with Oracle Text indexes.
     */
    @Test
    public void text() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/text");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for clean together with Index Organized Tables.
     */
    @Test
    public void indexOrganizedTable() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/iot");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for clean together with Nested Tables.
     */
    @Test
    public void nestedTable() throws FlywayException {
        flyway.setLocations("migration/dbsupport/oracle/sql/nested");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }
}
