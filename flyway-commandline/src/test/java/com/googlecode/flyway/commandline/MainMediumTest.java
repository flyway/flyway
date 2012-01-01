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
package com.googlecode.flyway.commandline;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Medium Test for Main.
 */
@SuppressWarnings({"JavaDoc"})
public class MainMediumTest {
    @Test
    public void loadConfigurationFile() throws Exception {
        Properties properties = new Properties();
        properties.put("existing", "still there!");
        properties.put("override", "loses :-(");

        String filename = new ClassPathResource("test.properties").getFile().getPath();
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
        assertFalse(new ClassPathResource("runtime.properties").exists());
        
        String folder = new ClassPathResource("dynamic").getFile().getPath();
        Main.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("runtime.properties").exists());
    }

    /**
     * Tests dynamically adding a jar file to the classpath.
     */
    @Test
    public void addJarToClasspath() throws Exception {
        assertFalse(new ClassPathResource("db/migration/V1.sql").exists());
        assertFalse(ClassUtils.isPresent("com.googlecode.flyway.sample.migration.V1_2__Another_user",
                Thread.currentThread().getContextClassLoader()));

        String jar = new ClassPathResource("flyway-sample.jar").getFile().getPath();
        Main.addJarOrDirectoryToClasspath(jar);

        assertTrue(new ClassPathResource("db/migration/V1.sql").exists());
        assertTrue(ClassUtils.isPresent("com.googlecode.flyway.sample.migration.V1_2__Another_user",
                Thread.currentThread().getContextClassLoader()));
    }
}
