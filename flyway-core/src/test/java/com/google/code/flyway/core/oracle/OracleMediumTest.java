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

package com.google.code.flyway.core.oracle;

import com.google.code.flyway.core.DbMigrator;
import com.google.code.flyway.core.SchemaVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:migration/oracle/oracle-context.xml"})
public class OracleMediumTest {
    @Autowired
    private DbMigrator dbMigrator;

    @Autowired
    private DataSource dataSource;

    @Test
    public void createAndMigrate() throws SQLException {
        SchemaVersion schemaVersion = dbMigrator.currentSchemaVersion();
        assertEquals("1.1", schemaVersion.getVersion());
        assertEquals("Populate table", schemaVersion.getDescription());
        assertTrue(dbMigrator.metaDataTableExists());

        SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        assertEquals("Mr. T triggered", jdbcTemplate.queryForObject("select name from test_user", String.class));
    }
}
