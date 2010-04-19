package com.google.code.flyway.core.oracle;

import com.google.code.flyway.core.DbMigrator;
import com.google.code.flyway.core.SchemaVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:migration/oracle/oracle-context.xml"})
public class OracleTest {
    @Autowired
    private DbMigrator dbMigrator;

    @Test
    public void createAndMigrate() throws SQLException {
        SchemaVersion schemaVersion = dbMigrator.currentSchemaVersion();
        Assert.assertEquals("1.1", schemaVersion.getVersion());
        Assert.assertEquals("Populate table", schemaVersion.getDescription());
        assertTrue(dbMigrator.metaDataTableExists());
    }
}
