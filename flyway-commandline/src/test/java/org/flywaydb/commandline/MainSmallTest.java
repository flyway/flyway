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
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals("user", Main.getArgumentProperty("-user=SA"));
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

        Main.overrideConfiguration(properties, args);

        assertEquals("SA", properties.getProperty("flyway.user"));
    }
}
