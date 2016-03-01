/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.commandline;

import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.logging.console.ConsoleLog.Level;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathScanner;
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
        oldClassLoader = getClassLoader();
        Main.initLogging(Level.INFO);
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

        String filename = new ClassPathResource("test.properties", getClassLoader()).getLocationOnDisk();
        String[] args = new String[]{"-configFile=" + filename, "-configFileEncoding=UTF-8"};

        Main.loadConfiguration(properties, args);

        assertEquals(4, properties.size());
        assertEquals("still there!", properties.getProperty("existing"));
        assertEquals("räbbit 123", properties.getProperty("roger"));
        assertEquals("wins :-)", properties.getProperty("override"));
    }

    @Test
    public void loadConfigurationFileBackslash() throws Exception {
        Properties properties = new Properties();

        String filename = new ClassPathResource("dynamic/pkg/runtime.conf", getClassLoader()).getLocationOnDisk();
        String[] args = new String[]{"-configFile=" + filename, "-configFileEncoding=UTF-8"};

        Main.loadConfiguration(properties, args);

        assertEquals(1, properties.size());
        assertEquals("at\\runtime", properties.getProperty("loaded"));
    }

    /**
     * Tests dynamically adding a directory to the classpath.
     */
    @Test
    public void addDirectoryToClasspath() throws Exception {
        assertFalse(new ClassPathResource("pkg/runtime.conf", getClassLoader()).exists());

        String folder = new ClassPathResource("dynamic", getClassLoader()).getLocationOnDisk();
        Main.addJarOrDirectoryToClasspath(folder);

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
        Main.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("funtime.properties", getClassLoader()).exists());

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:"), "fun", ".properties");
        assertEquals("funtime.properties", resources[1].getLocation());
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
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
        Main.addJarOrDirectoryToClasspath(jar);

        assertTrue(new ClassPathResource("db/migration/V1__Initial_structure.sql", getClassLoader()).exists());
        assertTrue(ClassUtils.isPresent("org.flywaydb.sample.migration.V1_2__Another_user", getClassLoader()));

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:db/migration"), "V1__", ".sql");
        assertEquals("db/migration/V1__Initial_structure.sql", resources[0].getLocation());

        Class<?>[] classes = new ClassPathScanner(getClassLoader()).scanForClasses(new Location("classpath:org/flywaydb/sample/migration"), SpringJdbcMigration.class);
        assertEquals("org.flywaydb.sample.migration.V1_2__Another_user", classes[0].getName());
    }
}
