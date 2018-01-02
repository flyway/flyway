/*
 * Copyright 2010-2018 Boxfuse GmbH
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

/**
 * Various abbreviation-related utilities.
 */
public class AbbreviationUtils {
    /**
     * Prevents instantiation.
     */
    private AbbreviationUtils() {
        // Do nothing.
    }

    /**
     * Abbreviates this description to a length that will fit in the database.
     *
     * @param description The description to process.
     * @return The abbreviated version.
     */
    public static String abbreviateDescription(String description) {
        if (description == null) {
            return null;
        }

        if (description.length() <= 200) {
            return description;
        }

        return description.substring(0, 197) + "...";
    }

    /**
     * Abbreviates this script to a length that will fit in the database.
     *
     * @param script The script to process.
     * @return The abbreviated version.
     */
    public static String abbreviateScript(String script) {
        if (script == null) {
            return null;
        }

        if (script.length() <= 1000) {
            return script;
        }

        return "..." + script.substring(3, 1000);
    }
}