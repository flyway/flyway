/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.util.scanner.classpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.internal.dbsupport.db2.DB2MigrationMediumTest;
import org.flywaydb.core.internal.resolver.jdbc.dummy.V2__InterfaceBasedMigration;
import org.flywaydb.core.internal.resolver.jdbc.dummy.V4__DummyExtendedAbstractJdbcMigration;
import org.flywaydb.core.internal.resolver.jdbc.dummy.Version3dot5;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.internal.creation.MockSettingsImpl;

/**
 * Tests for ClassPathScanner.
 */
public class ClassPathScannerSmallTest {
    private ClassPathScanner classPathScanner = new ClassPathScanner(Thread.currentThread().getContextClassLoader());

    @Test
    public void scanForResources() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:migration/sql"), "V", ".sql");

        assertEquals(4, resources.length);

        assertEquals("migration/sql/V1.1__View.sql", resources[0].getLocation());
        assertEquals("migration/sql/V1_2__Populate_table.sql", resources[1].getLocation());
        assertEquals("migration/sql/V1__First.sql", resources[2].getLocation());
        assertEquals("migration/sql/V2_0__Add_foreign_key_and_super_mega_humongous_padding_to_exceed_the_maximum_column_length_in_the_metadata_table.sql", resources[3].getLocation());
    }

    @Test
    public void scanForResourcesRoot() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:"), "CheckValidate", ".sql");

        // changed to 3 as new test cases are added for SybaseASE and DB2 z/OS
        assertEquals(3, resources.length);

        Set<String> validPaths = new HashSet<String>();
        validPaths.add("migration/validate/CheckValidate1__First.sql");
        validPaths.add("migration/dbsupport/sybaseASE/validate/CheckValidate1__First.sql");
        validPaths.add("migration/dbsupport/db2zos/sql/validate/CheckValidate1_1__First.sql");

        assertEquals(true, validPaths.contains(resources[0].getLocation()));
        assertEquals(true, validPaths.contains(resources[1].getLocation()));
        assertEquals(true, validPaths.contains(resources[2].getLocation()));
    }

    @Test
    public void scanForResourcesSomewhereInSubDir() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:migration"), "CheckValidate", ".sql");

        // changed to 3 as new test cases are added for SybaseASE and DB2 z/OS
        assertEquals(3, resources.length);

        Set<String> validPaths = new HashSet<String>();
        validPaths.add("migration/dbsupport/sybaseASE/validate/CheckValidate1__First.sql");
        validPaths.add("migration/validate/CheckValidate1__First.sql");
        validPaths.add("migration/dbsupport/db2zos/sql/validate/CheckValidate1_1__First.sql");

        assertEquals(true, validPaths.contains(resources[0].getLocation()));
        assertEquals(true, validPaths.contains(resources[1].getLocation()));
        assertEquals(true, validPaths.contains(resources[2].getLocation()));
    }

    @Test
    public void scanForResourcesDefaultPackage() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:"), "logback", "");

        assertEquals(1, resources.length);

        assertEquals("logback.xml", resources[0].getLocation());
    }

    @Test
    public void scanForResourcesSubDirectory() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:migration/subdir"), "V", ".sql");

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
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:org/flywaydb/core/internal/dbsupport"), "create", ".sql");

        assertTrue(resources.length > 7);

        assertEquals("org/flywaydb/core/internal/dbsupport/db2/createMetaDataTable.sql", resources[0].getLocation());
    }

    @Test
    public void scanForResourcesJarFile() throws Exception {
        Resource[] resources = classPathScanner.scanForResources(new Location("classpath:org/junit"), "Af", ".class");

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
        Class<?>[] classes = classPathScanner.scanForClasses(new Location("classpath:org/flywaydb/core/internal/dbsupport"), MigrationTestCase.class);

        assertTrue(classes.length >= 10);

        assertEquals(DB2MigrationMediumTest.class, classes[0]);
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
