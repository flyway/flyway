/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for replacing placeholders.
 */
public class PlaceholderReplacer {
    /**
     * PlaceholderReplacer that doesn't replace any placeholders.
     */
    public static final PlaceholderReplacer NO_PLACEHOLDERS = new PlaceholderReplacer(new HashMap<String, String>(), "", "");

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     */
    private final Map<String, String> placeholders;

    /**
     * The prefix of every placeholder. Usually ${
     */
    private final String placeholderPrefix;

    /**
     * The suffix of every placeholder. Usually }
     */
    private final String placeholderSuffix;

    /**
     * Creates a new PlaceholderReplacer.
     *
     * @param placeholders      A map of <placeholder, replacementValue> to apply to sql migration scripts.
     * @param placeholderPrefix The prefix of every placeholder. Usually ${
     * @param placeholderSuffix The suffix of every placeholder. Usually }
     */
    public PlaceholderReplacer(Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix) {
        this.placeholders = placeholders;
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Replaces the placeholders in this input string with their corresponding values.
     *
     * @param input The input to process.
     *
     * @return The input string with all placeholders replaced.
     */
    public String replacePlaceholders(String input) {
        String noPlaceholders = input;

        for (String placeholder : placeholders.keySet()) {
            String searchTerm = placeholderPrefix + placeholder + placeholderSuffix;
            noPlaceholders = StringUtils.replaceAll(noPlaceholders, searchTerm, placeholders.get(placeholder));
        }

        return noPlaceholders;
    }
}
