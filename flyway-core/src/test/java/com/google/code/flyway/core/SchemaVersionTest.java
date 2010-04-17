package com.google.code.flyway.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests SchemaVersion.
 */
public class SchemaVersionTest {
    @Test
    public void compareTo() {
        SchemaVersion v1 = new SchemaVersion("1");
        SchemaVersion v10 = new SchemaVersion("1.0");
        SchemaVersion v11 = new SchemaVersion("1.1");
        SchemaVersion v1100 = new SchemaVersion("1.1.0.0");
        SchemaVersion v1101 = new SchemaVersion("1.1.0.1");
        SchemaVersion v2 = new SchemaVersion("2");
        SchemaVersion v201004171859 = new SchemaVersion("201004171859");
        SchemaVersion v201004180000 = new SchemaVersion("201004180000");

        assertTrue(v1.compareTo(v10) == 0);
        assertTrue(v10.compareTo(v1) == 0);
        assertTrue(v1.compareTo(v11) < 0);
        assertTrue(v11.compareTo(v1) > 0);
        assertTrue(v11.compareTo(v1100) == 0);
        assertTrue(v1100.compareTo(v11) == 0);
        assertTrue(v11.compareTo(v1101) < 0);
        assertTrue(v1101.compareTo(v11) > 0);
        assertTrue(v1101.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1101) > 0);
        assertTrue(v201004171859.compareTo(v201004180000) < 0);
        assertTrue(v201004180000.compareTo(v201004171859) > 0);

        assertTrue(v2.compareTo(SchemaVersion.LATEST) < 0);
        assertTrue(SchemaVersion.LATEST.compareTo(v2) > 0);
        assertTrue(v201004180000.compareTo(SchemaVersion.LATEST) < 0);
        assertTrue(SchemaVersion.LATEST.compareTo(v201004180000) > 0);
    }
}
