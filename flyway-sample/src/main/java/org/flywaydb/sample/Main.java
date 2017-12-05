/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
