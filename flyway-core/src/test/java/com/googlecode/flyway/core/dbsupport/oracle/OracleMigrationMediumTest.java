/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@SuppressWarnings({"JavaDoc"})
@ContextConfiguration(locations = {"classpath:migration/oracle/oracle-context.xml"})
public class OracleMigrationMediumTest extends MigrationTestCase {
    @Autowired
    private DataSource dataSource;

    @Override
    protected String getBaseDir() {
        return "migration/sql";
    }

    @Override
    protected DbSupport getDbSupport(JdbcTemplate jdbcTemplate) {
        return new OracleDbSupport(jdbcTemplate);
    }

    /**
     * Tests migrations containing placeholders.
     */
    @Test
    public void migrationsWithPlaceholders() throws Exception {
        SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        int countUserObjects1 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", "test_user");
        flyway.setPlaceholders(placeholders);
        flyway.setBaseDir("migration/oracle/sql/placeholders");

        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1.1", schemaVersion.toString());
        assertEquals("Populate table", flyway.status().getDescription());

        assertEquals("Mr. T triggered", jdbcTemplate.queryForObject("select name from test_user", String.class));

        flyway.clean();

        int countUserObjects2 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");
        assertEquals(countUserObjects1, countUserObjects2);

        final List<MetaDataTableRow> metaDataTableRows = flyway.history();
        for (MetaDataTableRow metaDataTableRow : metaDataTableRows) {
            Assert.assertNotNull(metaDataTableRow.getScript() + " has no checksum", metaDataTableRow.getChecksum());
        }

    }

    /**
     * Tests clean for Oracle Spatial Extensions.
     */
    @Test
    public void cleanSpatialExtensions() throws Exception {
        flyway.setBaseDir("migration/oracle/sql/spatial");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Test clean with recycle bin
     */
    @Test
    public void cleanWithRecycleBin() {
        flyway.clean();
        SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        final int recyclebinCount1 = jdbcTemplate.queryForInt("select count(*) from recyclebin");
        // in SYSTEM tablespace the recycle bin is deactivated
        jdbcTemplate.update("CREATE TABLE test_user (name VARCHAR(25) NOT NULL,  PRIMARY KEY(name)) tablespace USERS");
        jdbcTemplate.update("DROP TABLE test_user");
        final int recyclebinCount2 = jdbcTemplate.queryForInt("select count(*) from recyclebin");
        Assert.assertTrue(recyclebinCount1 < recyclebinCount2);
        flyway.clean();        
    }
}
