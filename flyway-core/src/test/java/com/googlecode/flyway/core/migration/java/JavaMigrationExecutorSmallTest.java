package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.java.dummy.V2__InterfaceBasedMigration;
import com.googlecode.flyway.core.migration.java.dummy.Version3dot5;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test the info extraction of the JavaMigrationExecutor.
 */
public class JavaMigrationExecutorSmallTest {
    @Test
    public void conventionOverConfiguration() {
        JavaMigrationExecutor javaMigrationExecutor = new JavaMigrationExecutor(new V2__InterfaceBasedMigration());
        assertEquals("2", javaMigrationExecutor.getVersion().toString());
        assertEquals("InterfaceBasedMigration", javaMigrationExecutor.getDescription());
        assertNull(javaMigrationExecutor.getChecksum());
    }

    @Test
    public void explicitInfo() {
        JavaMigrationExecutor javaMigrationExecutor = new JavaMigrationExecutor(new Version3dot5());
        assertEquals("3.5", javaMigrationExecutor.getVersion().toString());
        assertEquals("Three Dot Five", javaMigrationExecutor.getDescription());
        assertEquals(35, javaMigrationExecutor.getChecksum().intValue());
    }
}
