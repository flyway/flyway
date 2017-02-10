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
package org.flywaydb.commandline;

import org.flywaydb.core.internal.util.logging.console.ConsoleLog.Level;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Medium Test for Main.
 */
@SuppressWarnings({"JavaDoc"})
public class MainClassLoaderSmallTest {
    @Before
    public void setUp() {
        Main.initLogging(Level.INFO);
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

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
