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
package org.flywaydb.sample;

import org.flywaydb.core.Flyway;
import org.flywaydb.sample.resolver.SampleResolver;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

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
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:hsqldb:file:db/flyway_sample;shutdown=true", "SA", "");
        flyway.setLocations("db/migration", "org.flywaydb.sample.migration");
        flyway.setResolvers(new SampleResolver());
        flyway.migrate();

        SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(flyway.getDataSource());
        List<Map<String, Object>> results = jdbcTemplate.queryForList("select name from test_user");
        for (Map<String, Object> result : results) {
            System.out.println("Name: " + result.get("NAME"));
        }
    }
}
