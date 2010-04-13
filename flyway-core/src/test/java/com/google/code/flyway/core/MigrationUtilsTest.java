package com.google.code.flyway.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test for MigrationUtils.
 */
public class MigrationUtilsTest {
    /**
     * Tests a class name that includes a description.
     */
    @Test
    public void extractSchemaVersionWithDescription() {
        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V9_4_EmailAxel");
        assertEquals(9, schemaVersion.getMajor());
        assertEquals(4, schemaVersion.getMinor());
    }

    /**
     * Tests a class name that includes a version with leading zeroes.
     */
    @Test
    public void extractSchemaVersionWithLeadingZeroes() {
        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V009_4_EmailAxel");
        assertEquals(9, schemaVersion.getMajor());
        assertEquals(4, schemaVersion.getMinor());
    }
}
