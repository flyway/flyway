package com.google.code.flyway.core.util;

import com.google.code.flyway.core.SchemaVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for MigrationUtils.
 */
public class MigrationUtilsTest {
    /**
     * Tests a schema version that lacks a description.
     */
    @Test
    public void extractSchemaVersionNoDescription() {
        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V9_4");
        assertEquals("9.4", schemaVersion.getVersion());
        assertNull(schemaVersion.getDescription());
    }

    /**
     * Tests a schema version that includes a description.
     */
    @Test
    public void extractSchemaVersionWithDescription() {
        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V9_4__EmailAxel");
        assertEquals("9.4", schemaVersion.getVersion());
        assertEquals("EmailAxel", schemaVersion.getDescription());
    }

    /**
     * Tests a schema version that includes a description with spaces.
     */
    @Test
    public void extractSchemaVersionWithDescriptionWithSpaces() {
        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V9_4__Big_jump");
        assertEquals("9.4", schemaVersion.getVersion());
        assertEquals("Big jump", schemaVersion.getDescription());
    }

    /**
     * Tests a schema version that includes a version with leading zeroes.
     */
    @Test
    public void extractSchemaVersionWithLeadingZeroes() {
        SchemaVersion schemaVersion = MigrationUtils.extractSchemaVersion("V009_4__EmailAxel");
        assertEquals("9.4", schemaVersion.getVersion());
        assertEquals("EmailAxel", schemaVersion.getDescription());
    }
}
