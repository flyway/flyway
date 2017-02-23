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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool for replacing placeholders.
 */
public class PlaceholderReplacer {
    /**
     * PlaceholderReplacer that doesn't replace any placeholders.
     */
    public static final PlaceholderReplacer NO_PLACEHOLDERS = new PlaceholderReplacer(new HashMap<String, String>(), "", "") {
        @Override
        public String replacePlaceholders(String input) {
            return input;
        }
    };

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
     * @return The input string with all placeholders replaced.
     */
    public String replacePlaceholders(String input) {
        String noPlaceholders = input;

        for (String placeholder : placeholders.keySet()) {
            String searchTerm = placeholderPrefix + placeholder + placeholderSuffix;
            String value = placeholders.get(placeholder);
            noPlaceholders = StringUtils.replaceAll(noPlaceholders, searchTerm, value == null ? "" : value);
        }
        checkForUnmatchedPlaceholderExpression(noPlaceholders);

        return noPlaceholders;
    }

    /**
     * Check for unmatched placeholder expressions in the input string and throw
     * a FlywayException if they do not have corresponding values.
     *
     * @param input The input string.
     * @throws FlywayException An exception listing the unmatched expressions.
     */
    private void checkForUnmatchedPlaceholderExpression(String input) {
        String regex = Pattern.quote(placeholderPrefix) + "(.+?)" + Pattern.quote(placeholderSuffix);
        Matcher matcher = Pattern.compile(regex).matcher(input);

        Set<String> unmatchedPlaceHolderExpressions = new TreeSet<String>();
        while (matcher.find()) {
            unmatchedPlaceHolderExpressions.add(matcher.group());
        }

        if (!unmatchedPlaceHolderExpressions.isEmpty()) {
            throw new FlywayException("No value provided for placeholder expressions: "
                    + StringUtils.collectionToCommaDelimitedString(unmatchedPlaceHolderExpressions)
                    + ".  Check your configuration!");
        }
    }
}
