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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathScanner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test for ClassUtils.
 */
public class ClassUtilsSmallTest {
    /**
     * The old classloader, to be restored after a test completes.
     */
    private static ClassLoader oldClassLoader;

    @BeforeClass
    public static void setUp() throws IOException {
        oldClassLoader = getClassLoader();
        String jar = new ClassPathResource("no-directory-entries.jar", getClassLoader()).getLocationOnDisk();
        assertTrue(new File(jar).isFile());
        ClassUtils.addJarOrDirectoryToClasspath(jar);
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @AfterClass
    public static void tearDown() {
        Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

    @Test
    public void isPresent() {
        assertTrue(ClassUtils.isPresent("org.flywaydb.core.Flyway", Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void isPresentNot() {
        assertFalse(ClassUtils.isPresent("com.example.FakeClass", Thread.currentThread().getContextClassLoader()));
    }

    /**
     * Tests dynamically adding a directory to the classpath.
     */
    @Test
    public void addDirectoryToClasspath() throws Exception {
        assertFalse(new ClassPathResource("pkg/runtime.conf", getClassLoader()).exists());

        String folder = new ClassPathResource("dynamic", getClassLoader()).getLocationOnDisk();
        ClassUtils.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("pkg/runtime.conf", getClassLoader()).exists());

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:pkg"), "run", ".conf");
        assertEquals("pkg/runtime.conf", resources[0].getLocation());
    }

    /**
     * Tests dynamically adding a directory to the default package of classpath.
     */
    @Test
    public void addDirectoryToClasspathDefaultPackage() throws Exception {
        assertFalse(new ClassPathResource("runtime.conf", getClassLoader()).exists());

        String folder = new ClassPathResource("dynamic/pkg2", getClassLoader()).getLocationOnDisk();
        ClassUtils.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("funtime.properties", getClassLoader()).exists());

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:"), "fun", ".properties");
        assertEquals("funtime.properties", resources[1].getLocation());
    }

    /**
     * Tests dynamically adding a jar file to the classpath.
     */
    @Test
    public void addJarToClasspath() throws Exception {
        assertFalse(new ClassPathResource("db/migration/V1__Initial_structure.sql.sql", getClassLoader()).exists());
        assertFalse(ClassUtils.isPresent("org.flywaydb.sample.migration.V1_2__Another_user", getClassLoader()));

        String jar = new ClassPathResource("flyway-sample.jar", getClassLoader()).getLocationOnDisk();
        assertTrue(new File(jar).isFile());
        ClassUtils.addJarOrDirectoryToClasspath(jar);

        assertTrue(new ClassPathResource("db/migration/V1__Initial_structure.sql", getClassLoader()).exists());
        assertTrue(ClassUtils.isPresent("org.flywaydb.sample.migration.V1_2__Another_user", getClassLoader()));

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:db/migration"), "V1__", ".sql");
        assertEquals("db/migration/V1__Initial_structure.sql", resources[0].getLocation());

        Class<?>[] classes = new ClassPathScanner(getClassLoader()).scanForClasses(new Location("classpath:org/flywaydb/sample/migration"), SpringJdbcMigration.class);
        assertEquals("org.flywaydb.sample.migration.V1_2__Another_user", classes[0].getName());
    }

    /**
     * Tests dynamically adding a jar file to the classpath.
     */
    @Test
    public void addJarToClasspathNoDirectoryEntries() throws Exception {
        assertTrue(new ClassPathResource("db/migration/V1_11__Create_tbl_bob.sql", getClassLoader()).exists());
        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:db/migration"), "V1_11", ".sql");
        Class[] classes = new ClassPathScanner(getClassLoader()).scanForClasses(new Location("classpath:db/migration"), JdbcMigration.class);

        assertEquals("db/migration/V1_11__Create_tbl_bob.sql", resources[0].getLocation());
        assertEquals(0, classes.length);
    }
}
