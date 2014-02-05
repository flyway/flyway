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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for PlaceholderReplacer.
 */
public class PlaceholderReplacerSmallTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test string to check functionality.
     */
    private static final String TEST_STR = "No ${placeholder} #[left] to ${replace}";

    @Test
    public void antStylePlaceholders() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("placeholder", "value");
        placeholders.put("replace", "be replaced");
        placeholders.put("dummy", "shouldNotAppear");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}", true);

        assertEquals("No value #[left] to be replaced", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void exoticPlaceholders() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("left", "right");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "#[", "]", true);

        assertEquals("No ${placeholder} right to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void unmatchedPlaceholdersNotStrict() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("placeholder", "value");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}", false);

        assertEquals("No value #[left] to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void unmatchedPlaceholdersStrict() throws FlywayException {
        thrown.expect(FlywayException.class);
        thrown.expectMessage("No value provided for placeholder expressions: #[left].  Check your configuration!");
        Map<String, String> placeholders = new HashMap<String, String>();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "#[", "]", true);
        placeholderReplacer.replacePlaceholders(TEST_STR);
    }

    @Test
    public void unmatchedPlaceholdersWithMultipleOccurencesNotStrict() {
        Map<String, String> placeholders = new HashMap<String, String>();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}", false);

        assertEquals("No ${placeholder} #[left] to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void unmatchedPlaceholdersWithMultipleOccurencesStrict() throws FlywayException {
        thrown.expect(FlywayException.class);
        thrown.expectMessage("No value provided for placeholder expressions: ${placeholder}, ${replace}.  Check your configuration!");
        Map<String, String> placeholders = new HashMap<String, String>();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}", true);
        placeholderReplacer.replacePlaceholders(TEST_STR + TEST_STR);
    }

    @Test
    public void mixedUnmatchedPlaceholdersWithPlaceholdersNotStrict() {
        Map<String, String> placeholders = new HashMap<String, String>();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}", false);
        placeholders.put("placeholder", "value");

        assertEquals("No value #[left] to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }
}
