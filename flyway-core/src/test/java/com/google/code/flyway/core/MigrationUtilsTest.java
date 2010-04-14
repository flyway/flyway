package com.google.code.flyway.core;

import static org.junit.Assert.assertEquals;

import com.google.code.flyway.core.util.MigrationUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for MigrationUtils.
 */
public class MigrationUtilsTest {
    /**
     * Tests a class name that includes a description.
     */
    @Ignore
    @Test
    public void extractSchemaVersionWithDescription() {
//        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V9_4_EmailAxel");
//        assertEquals("9.4", schemaVersion.toString());
//        assertEquals("EmailAxel", schemaVersion.getMinor());
    }

    /**
     * Tests a class name that includes a version with leading zeroes.
     */
    @Ignore
    @Test
    public void extractSchemaVersionWithLeadingZeroes() {
//        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V009_4_EmailAxel");
//        assertEquals(9, schemaVersion.getMajor());
//        assertEquals(4, schemaVersion.getMinor());
    }
}
