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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.logging.console.ConsoleLog.Level;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test for Main.
 */
public class MainSmallTest {
    @Before
    public void setUp() {
        Main.initLogging(Level.INFO);
    }

    @Test
    public void isPropertyArgument() {
        assertTrue(Main.isPropertyArgument("-user=SA"));
        assertFalse(Main.isPropertyArgument("baseline"));
    }

    @Test
    public void getArgumentProperty() {
        assertEquals(ConfigUtils.USER, Main.getArgumentProperty("-user=SA"));
    }

    @Test
    public void getArgumentValue() {
        assertEquals("SA", Main.getArgumentValue("-user=SA"));
        assertEquals("", Main.getArgumentValue("-password="));
    }

    @Test
    public void overrideConfiguration() {
        Properties properties = new Properties();
        String[] args = new String[]{"-user=SA"};

        Main.overrideConfigurationWithArgs(properties, args);

        assertEquals("SA", properties.getProperty("flyway.user"));
    }

    @Test
    public void validateArgs() {
        String[] args = new String[]{"-url=jdbc:mydb", "migrate"};
        Main.validateArgs(args);
    }

    @Test
    public void validateArgsInvalid() {
        String invalidArg = "-dryRunOutput:previewDeployment.sql";
        String[] args = new String[]{invalidArg, "migrate"};

        try {
            Main.validateArgs(args);
        } catch (FlywayException e) {
            assertTrue(e.getMessage().contains(invalidArg));
        }
    }
}
