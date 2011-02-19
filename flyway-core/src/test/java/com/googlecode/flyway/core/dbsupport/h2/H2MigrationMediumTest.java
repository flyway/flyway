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
package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@ContextConfiguration(locations = {"classpath:migration/h2/h2-context.xml"})
public class H2MigrationMediumTest extends MigrationTestCase {
    @Override
    protected String getBaseDir() {
        return "migration/sql";
    }

    @Override
    protected String getQuoteBaseDir() {
        return "migration/quote";
    }

    @Override
    protected DbSupport getDbSupport(JdbcTemplate jdbcTemplate) {
        return new H2DbSupport(jdbcTemplate);
    }

    @Test
    public void dollarQuotedString() {
        flyway.setBaseDir("migration/h2/sql/dollar_quoted_string");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1.1", schemaVersion.toString());
        assertEquals("Populate table", flyway.status().getDescription());

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select name from test_user");
        for (Map<String, Object> row : rows) {
            System.out.println("Name: " + row.get("NAME"));
        }

        assertEquals("'Mr. Semicolon+Linebreak;\nanother line'",
                jdbcTemplate.queryForObject("select name from test_user where name like '%line'''", String.class));
    }
}