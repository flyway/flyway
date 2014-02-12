/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.commandline;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.ClassUtils;
import com.googlecode.flyway.core.util.Resource;
import com.googlecode.flyway.core.util.scanner.classpath.ClassPathScanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Medium Test for Main.
 */
@SuppressWarnings({"JavaDoc"})
public class MainClassLoaderSmallTest {
    /**
     * The old classloader, to be restored after a test completes.
     */
    private ClassLoader oldClassLoader;

    @Before
    public void setUp() {
        oldClassLoader = Thread.currentThread().getContextClassLoader();
        Main.initLogging(false);
    }

    @After
    public void tearDown() {
        Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

    @Test
    public void loadConfigurationFile() throws Exception {
        Properties properties = new Properties();
        properties.put("existing", "still there!");
        properties.put("override", "loses :-(");

        String filename = new ClassPathResource("test.properties").getLocationOnDisk();
        String[] args = new String[]{"-configFile=" + filename, "-configFileEncoding=UTF-8"};

        Main.loadConfigurationFile(properties, args);

        assertEquals(4, properties.size());
        assertEquals("still there!", properties.getProperty("existing"));
        assertEquals("räbbit 123", properties.getProperty("roger"));
        assertEquals("wins :-)", properties.getProperty("override"));
    }

    /**
     * Tests dynamically adding a directory to the classpath.
     */
    @Test
    public void addDirectoryToClasspath() throws Exception {
        assertFalse(new ClassPathResource("pkg/runtime.properties").exists());

        String folder = new ClassPathResource("dynamic").getLocationOnDisk();
        Main.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("pkg/runtime.properties").exists());

        Resource[] resources = new ClassPathScanner().scanForResources("pkg", "run", ".properties");
        assertEquals("pkg/runtime.properties", resources[0].getLocation());
    }

    /**
     * Tests dynamically adding a directory to the default package of classpath.
     */
    @Test
    public void addDirectoryToClasspathDefaultPackage() throws Exception {
        assertFalse(new ClassPathResource("runtime.properties").exists());

        String folder = new ClassPathResource("dynamic/pkg").getLocationOnDisk();
        Main.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("runtime.properties").exists());

        Resource[] resources = new ClassPathScanner().scanForResources("", "run", ".properties");
        assertEquals("runtime.properties", resources[1].getLocation());
    }

    /**
     * Tests dynamically adding a jar file to the classpath.
     */
    @Test
    public void addJarToClasspath() throws Exception {
        assertFalse(new ClassPathResource("db/migration/V1__Initial_structure.sql.sql").exists());
        assertFalse(ClassUtils.isPresent("com.googlecode.flyway.sample.migration.V1_2__Another_user"));

        String jar = new ClassPathResource("flyway-sample.jar").getLocationOnDisk();
        assertTrue(new File(jar).isFile());
        Main.addJarOrDirectoryToClasspath(jar);

        assertTrue(new ClassPathResource("db/migration/V1__Initial_structure.sql").exists());
        assertTrue(ClassUtils.isPresent("com.googlecode.flyway.sample.migration.V1_2__Another_user"));

        Resource[] resources = new ClassPathScanner().scanForResources("db/migration", "V1__", ".sql");
        assertEquals("db/migration/V1__Initial_structure.sql", resources[0].getLocation());

        Class<?>[] classes = new ClassPathScanner().scanForClasses("com/googlecode/flyway/sample/migration", SpringJdbcMigration.class);
        assertEquals("com.googlecode.flyway.sample.migration.V1_2__Another_user", classes[0].getName());
    }
}
