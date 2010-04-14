package com.google.code.flyway.core;

import com.google.code.flyway.core.util.MigrationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/mysql-context.xml"})
public class MySqlTest {
    @Autowired
    @Qualifier("root-simpleJdbcTemplate")
    private SimpleJdbcTemplate rootSimpleJdbcTemplate;

    @Before
    public void createDatabase() {
        MigrationUtils.executeSqlScript(rootSimpleJdbcTemplate, new ClassPathResource("migration/mysql/createDatabase.sql"));
    }

    @After
    public void dropDatabase() {
        MigrationUtils.executeSqlScript(rootSimpleJdbcTemplate, new ClassPathResource("migration/mysql/dropDatabase.sql"));
    }

    @Test
    public void createAndMigrate() {

    }
}
