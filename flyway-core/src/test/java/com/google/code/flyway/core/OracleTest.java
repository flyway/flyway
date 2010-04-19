package com.google.code.flyway.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/oracle-context.xml"})
public class OracleTest {
    @Autowired
    private DbMigrator dbMigrator;

    @Test
    public void createAndMigrate() throws SQLException {
        assertEquals(new SchemaVersion("1"), dbMigrator.currentSchemaVersion());
        assertTrue(dbMigrator.metaDataTableExists());
    }
}
