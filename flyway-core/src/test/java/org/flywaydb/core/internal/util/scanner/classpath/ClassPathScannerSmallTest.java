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
package org.flywaydb.core.internal.util.scanner.classpath;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.internal.resolver.jdbc.dummy.V2__InterfaceBasedMigration;
import org.flywaydb.core.internal.resolver.jdbc.dummy.V4__DummyExtendedAbstractJdbcMigration;
import org.flywaydb.core.internal.resolver.jdbc.dummy.Version3dot5;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.internal.creation.MockSettingsImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for ClassPathScanner.
 */
public class ClassPathScannerSmallTest {
    private ClassPathScanner classPathScanner = new ClassPathScanner(Thread.currentThread().getContextClassLoader());

    @Test
    public void scanForResources() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(new Location("classpath:migration/sql"), "V", ".sql");

        assertEquals(4, resources.length);

        assertEquals("migration/sql/V1.1__View.sql", resources[0].getLocation());
        assertEquals("migration/sql/V1_2__Populate_table.sql", resources[1].getLocation());
        assertEquals("migration/sql/V1__First.sql", resources[2].getLocation());
        assertEquals("migration/sql/V2_0__Add_foreign_key_and_super_mega_humongous_padding_to_exceed_the_maximum_column_length_in_the_metadata_table.sql", resources[3].getLocation());
    }

    @Test
    public void scanForResourcesRoot() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(new Location("classpath:"), "CheckValidate", ".sql");

        assertEquals(2, resources.length);

        Set<String> validPaths = new HashSet<String>();
        validPaths.add("migration/validate/CheckValidate1__First.sql");

        assertEquals(true, validPaths.contains(resources[1].getLocation()));
    }

    @Test
    public void scanForResourcesSomewhereInSubDir() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(
                new Location("classpath:migration"), "CheckValidate", ".sql");

        assertEquals(2, resources.length);

        Set<String> validPaths = new HashSet<String>();
        validPaths.add("migration/validate/CheckValidate1__First.sql");

        assertEquals(true, validPaths.contains(resources[1].getLocation()));
    }

    @Test
    public void scanForResourcesDefaultPackage() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(new Location("classpath:"), "logback", "");

        assertEquals(1, resources.length);

        assertEquals("logback.xml", resources[0].getLocation());
    }

    @Test
    public void scanForResourcesSubDirectory() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(new Location("classpath:migration/subdir"), "V", ".sql");

        assertEquals(3, resources.length);

        assertEquals("migration/subdir/V1_1__Populate_table.sql", resources[0].getLocation());
        assertEquals("migration/subdir/dir1/V1__First.sql", resources[1].getLocation());
        assertEquals("migration/subdir/dir2/V2_0__Add_foreign_key.sql", resources[2].getLocation());
    }

    @Test
    public void scanForResourcesInvalidPath() throws Exception {
        classPathScanner.scanForResources(new Location("classpath:invalid"), "V", ".sql");
    }

    @Test
    public void scanForResourcesSplitDirectory() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(new Location("classpath:org/flywaydb/core/internal/database"), "create", ".sql");

        assertTrue(resources.length > 7);

        assertEquals("org/flywaydb/core/internal/database/cockroachdb/createMetaDataTable.sql", resources[0].getLocation());
    }

    @Test
    public void scanForResourcesJarFile() throws Exception {
        LoadableResource[] resources = classPathScanner.scanForResources(new Location("classpath:org/junit"), "Af", ".class");

        assertEquals(2, resources.length);

        assertEquals("org/junit/After.class", resources[0].getLocation());
        assertEquals("org/junit/AfterClass.class", resources[1].getLocation());
    }

    @Test
    public void scanForClasses() throws Exception {
        Class<?>[] classes = classPathScanner.scanForClasses(new Location("classpath:org/flywaydb/core/internal/resolver/jdbc/dummy"), JdbcMigration.class);

        assertEquals(3, classes.length);

        assertEquals(V2__InterfaceBasedMigration.class, classes[0]);
        assertEquals(Version3dot5.class, classes[2]);
        assertEquals(V4__DummyExtendedAbstractJdbcMigration.class, classes[1]);
    }

    @Test
    public void scanForClassesSubPackage() throws Exception {
        Class<?>[] classes = classPathScanner.scanForClasses(new Location("classpath:org/flywaydb/core/internal"), MigrationChecksumProvider.class);

        assertEquals(3, classes.length);
        assertEquals(Version3dot5.class, classes[1]);
    }

    @Test
    public void scanForClassesSplitPackage() throws Exception {
        Class<?>[] classes = classPathScanner.scanForClasses(new Location("classpath:org/flywaydb/core/internal/util"), UrlResolver.class);

        assertTrue(classes.length >= 2);
        assertTrue(Arrays.asList(classes).contains(JBossVFSv2UrlResolver.class));
    }

    @Test
    public void scanForClassesJarFile() throws Exception {
        Class<?>[] classes = classPathScanner.scanForClasses(new Location("classpath:org/mockito/internal/creation"), MockSettings.class);

        assertTrue(Arrays.asList(classes).contains(MockSettingsImpl.class));
    }

    @Test
    public void scanForSpecificPathWhenMultiplePathsExist() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:net/sourceforge/jtds/jdbc"), "", ".class");
        for (Resource resource : resources) {
            assertFalse(resource.getLocation(), resource.getLocation().startsWith("net/sourceforge/jtds/jdbcx/"));
        }
    }
}
