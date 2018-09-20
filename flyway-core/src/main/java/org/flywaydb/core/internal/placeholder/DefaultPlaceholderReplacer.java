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
package org.flywaydb.core.internal.placeholder;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool for replacing placeholders.
 */
public class DefaultPlaceholderReplacer implements PlaceholderReplacer {
    /**
     * A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
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
     * Regex pattern that matches any placeholder.
     */
    private final Pattern anyPlaceholderPattern;

    /**
     * Creates a new PlaceholderReplacer.
     *
     * @param placeholders      A map of <placeholder, replacementValue> to apply to sql migration scripts.
     * @param placeholderPrefix The prefix of every placeholder. Usually ${
     * @param placeholderSuffix The suffix of every placeholder. Usually }
     */
    public DefaultPlaceholderReplacer(Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix) {
        this.placeholders = placeholders;
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        this.anyPlaceholderPattern = Pattern.compile(Pattern.quote(placeholderPrefix) + "(.+?)" + Pattern.quote(placeholderSuffix));
    }

    @Override
    public Map<String, String> getPlaceholderReplacements() {
        return placeholders;
    }

    @Override
    public String replacePlaceholders(String input) {
        String noPlaceholders = input;

        for (Map.Entry<String, String> placeholder : getPlaceholderReplacements().entrySet()) {
            String searchTerm = placeholderPrefix + placeholder.getKey() + placeholderSuffix;
            String value = placeholder.getValue() == null ? "" : placeholder.getValue();
            noPlaceholders = StringUtils.replace(noPlaceholders, searchTerm, value);
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
        Matcher matcher = anyPlaceholderPattern.matcher(input);

        Set<String> unmatchedPlaceHolderExpressions = new TreeSet<>();
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