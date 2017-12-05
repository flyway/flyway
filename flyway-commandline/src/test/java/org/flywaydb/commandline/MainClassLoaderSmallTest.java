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
package org.flywaydb.commandline;

import org.flywaydb.core.internal.util.logging.console.ConsoleLog.Level;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
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

        Main.loadConfigurationFromConfigFiles(properties, args, new HashMap<String, String>());

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

        Main.loadConfigurationFromConfigFiles(properties, args, new HashMap<String, String>());

        assertEquals(1, properties.size());
        assertEquals("at\\runtime", properties.getProperty("loaded"));
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
