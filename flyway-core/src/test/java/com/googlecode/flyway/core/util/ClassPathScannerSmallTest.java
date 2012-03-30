/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.db2.DB2MigrationMediumTest;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.java.JavaMigration;
import com.googlecode.flyway.core.migration.java.dummy.V2__InterfaceBasedMigration;
import com.googlecode.flyway.core.migration.java.dummy.Version3dot5;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.DescribedAs;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ClassPathScanner.
 */
public class ClassPathScannerSmallTest {
    @Test
    public void scanForResources() throws Exception {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("migration/sql", "V", ".sql");

        assertEquals(4, resources.length);

        assertEquals("migration/sql/V1_1__View.sql", resources[0].getLocation());
        assertEquals("migration/sql/V1_2__Populate_table.sql", resources[1].getLocation());
        assertEquals("migration/sql/V1__First.sql", resources[2].getLocation());
        assertEquals("migration/sql/V2_0__Add_foreign_key_and_super_mega_humongous_padding_to_exceed_the_maximum_column_length_in_the_metadata_table.sql", resources[3].getLocation());
    }

    @Test
    public void scanForResourcesRoot() throws Exception {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("", "CheckValidate", ".sql");

        assertEquals(1, resources.length);

        assertEquals("migration/validate/CheckValidate1__First.sql", resources[0].getLocation());
    }

    @Test
    public void scanForResourcesDefaultPackage() throws Exception {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("", "log4j", "");

        assertEquals(2, resources.length);

        assertEquals("log4j.dtd", resources[0].getLocation());
        assertEquals("log4j.xml", resources[1].getLocation());
    }

    @Test
    public void scanForResourcesLeadingSlash() throws Exception {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("/migration/sql", "V", ".sql");

        assertEquals(4, resources.length);

        assertEquals("migration/sql/V1_1__View.sql", resources[0].getLocation());
        assertEquals("migration/sql/V1_2__Populate_table.sql", resources[1].getLocation());
        assertEquals("migration/sql/V1__First.sql", resources[2].getLocation());
        assertEquals("migration/sql/V2_0__Add_foreign_key_and_super_mega_humongous_padding_to_exceed_the_maximum_column_length_in_the_metadata_table.sql", resources[3].getLocation());
    }

    @Test
    public void scanForResourcesSubDirectory() throws Exception {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("migration/subdir", "V", ".sql");

        assertEquals(3, resources.length);

        assertEquals("migration/subdir/V1_1__Populate_table.sql", resources[0].getLocation());
        assertEquals("migration/subdir/dir1/V1__First.sql", resources[1].getLocation());
        assertEquals("migration/subdir/dir2/V2_0__Add_foreign_key.sql", resources[2].getLocation());
    }

    @Test
    public void scanForResourcesSplitDirectory() throws Exception {
        ClassPathResource[] resources =
                new ClassPathScanner().scanForResources("com/googlecode/flyway/core/dbsupport", "create", ".sql");

        assertTrue(resources.length > 7);

        assertEquals("com/googlecode/flyway/core/dbsupport/db2/createMetaDataTable.sql", resources[0].getLocation());
    }

    @Test
    public void scanForResourcesJarFile() throws Exception {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("junit", "", ".gif");

        assertEquals(2, resources.length);

        assertEquals("junit/runner/logo.gif", resources[0].getLocation());
        assertEquals("junit/runner/smalllogo.gif", resources[1].getLocation());
    }

    @Test
    public void scanForClasses() throws Exception {
        Class<?>[] classes = new ClassPathScanner().scanForClasses("com.googlecode.flyway.core.migration.java.dummy", JavaMigration.class);

        assertEquals(2, classes.length);

        assertEquals(V2__InterfaceBasedMigration.class, classes[0]);
        assertEquals(Version3dot5.class, classes[1]);
    }

    @Test
    public void scanForClassesSubPackage() throws Exception {
        Class<?>[] classes = new ClassPathScanner().scanForClasses("com.googlecode.flyway.core.dbsupport", MigrationTestCase.class);

        assertTrue(classes.length > 11);

        assertEquals(DB2MigrationMediumTest.class, classes[0]);
    }

    @Test
    public void scanForClassesSplitPackage() throws Exception {
        Class<?>[] classes = new ClassPathScanner().scanForClasses("com.googlecode.flyway.core.dbsupport", DbSupport.class);

        assertTrue(classes.length > 8);

        assertEquals(DbSupport.class, classes[0]);
    }

    @Test
    public void scanForClassesJarFile() throws Exception {
        Class<?>[] classes = new ClassPathScanner().scanForClasses("org.hamcrest.core", Matcher.class);

        assertEquals(10, classes.length);

        assertEquals(AllOf.class, classes[0]);
        assertEquals(AnyOf.class, classes[1]);
        assertEquals(DescribedAs.class, classes[2]);
    }
}
