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

package com.google.code.flyway.sample;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.google.code.flyway.core.Flyway;

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
        flyway.migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String name = (String) jdbcTemplate.queryForObject("select name from test_user", String.class);
        System.out.println("Name: " + name);
    }
}
