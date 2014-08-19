/**
 * Copyright 2010-2014 Axel Fontaine
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

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test for PropertiesUtils.
 */
public class PropertiesUtilsSmallTest {
    @Test
    public void getIntProperty() {
        Properties properties = new Properties();
        properties.setProperty("consoleWidth", "");
        assertEquals(80, PropertiesUtils.getIntProperty(properties, "consoleWidth", 80));
    }

    @Test
    public void getIntPropertyNonInt() {
        Properties properties = new Properties();
        properties.setProperty("consoleWidth", "wrong!");
        assertEquals(80, PropertiesUtils.getIntProperty(properties, "consoleWidth", 80));
    }

    @Test
    public void getIntPropertyBlank() {
        Properties properties = new Properties();
        properties.setProperty("consoleWidth", "    ");
        assertEquals(80, PropertiesUtils.getIntProperty(properties, "consoleWidth", 80));
    }

    @Test
    public void getIntPropertyTab() {
        Properties properties = new Properties();
        properties.setProperty("consoleWidth", "\t");
        assertEquals(80, PropertiesUtils.getIntProperty(properties, "consoleWidth", 80));
    }
}
