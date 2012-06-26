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
package com.googlecode.flyway.sample;

import com.googlecode.flyway.core.Flyway;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Simplest possible sample to demonstrate the usage of Flyway.
 */
public class Main {
    /**
     * Runs the sample.
     *
     * @param args None supported.
     */
    public static void main(String[] args) throws Exception {
        DataSource dataSource =
                new SimpleDriverDataSource(new org.hsqldb.jdbcDriver(), "jdbc:hsqldb:file:db/flyway_sample;shutdown=true", "SA", "");
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("com.googlecode.flyway.sample.migration");
        flyway.migrate();

        SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        List<Map<String, Object>> results = jdbcTemplate.queryForList("select name from test_user");
        for (Map<String, Object> result : results) {
            System.out.println("Name: " + result.get("NAME"));
        }
    }
}
