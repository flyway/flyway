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

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/mysql-context.xml"})
public class MySQLTest {
    @Autowired
    private DbMigrator dbMigrator;

    @Test
    public void createAndMigrate() {
        assertEquals(new SchemaVersion("1"), dbMigrator.currentSchemaVersion());
    }
}
