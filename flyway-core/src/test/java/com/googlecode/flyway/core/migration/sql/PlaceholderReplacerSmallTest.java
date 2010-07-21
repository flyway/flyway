/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.migration.sql;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for PlaceholderReplacer.
 */
public class PlaceholderReplacerSmallTest {
    /**
     * Test string to check functionality.
     */
    private static final String TEST_STR = "No ${placeholder} #[left] to ${replace}";

    @Test
    public void noPlaceholders() {
        assertEquals(TEST_STR, PlaceholderReplacer.NO_PLACEHOLDERS.replacePlaceholders(TEST_STR));
    }

    @Test
    public void antStylePlaceholders() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("placeholder", "value");
        placeholders.put("dummy", "shouldNotAppear");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        assertEquals("No value #[left] to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void exoticPlaceholders() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("left", "right");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "#[", "]");

        assertEquals("No ${placeholder} right to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }
}
