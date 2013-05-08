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

import com.googlecode.flyway.core.api.FlywayException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * The regexp which is used to find placeholder expressions.
     */
    private final Pattern placeHolderRegexp;

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
        this.placeHolderRegexp = Pattern.compile(Pattern.quote(placeholderPrefix) + "(.*?)" +
                Pattern.quote(placeholderSuffix));
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

        checkForUnmatchedPlaceholderExpression(input);
        for (String placeholder : placeholders.keySet()) {
            String searchTerm = placeholderPrefix + placeholder + placeholderSuffix;
            noPlaceholders = StringUtils.replaceAll(noPlaceholders, searchTerm, placeholders.get(placeholder));
        }

        return noPlaceholders;
    }

    /**
     * Check for unmatched placeholder expressions in the input string and throw
     * a FlywayException if they do not have corresponding values.
     *
     * @param input The input string.
     *
     * @throws FlywayException An exception listing the unmatched expressions.
     */
    private void checkForUnmatchedPlaceholderExpression(String input){
        List<String> placeHolderExpressions = findPlaceholderExpressions(input);
        placeHolderExpressions.removeAll(placeholders.keySet());
        if(!placeHolderExpressions.isEmpty()){
            String msg = buildUnmatchedPlaceholdersErrorMsg(placeHolderExpressions);
            throw new FlywayException(msg);
        }

    }

    /**
     * Finds all placeholders referenced in placeholder expressions in a given input string.
     *
     * @param input The input to search.
     *
     * @return The sorted list of unique placeholder names referenced in the input.
     */
    private List<String> findPlaceholderExpressions(String input) {
        List<String> matches = new ArrayList();
        Matcher matcher = placeHolderRegexp.matcher(input);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        List unmatched = new ArrayList<String>(new HashSet<String>(matches));
        Collections.sort(unmatched);
        return unmatched;
    }


    /**
     * Build the error message when unmatched placeholder expressions exist.
     *
     * @param placeHolderExpressions The placeholder expressions found in the script.
     *
     * @return The error message.
     */
    private String buildUnmatchedPlaceholdersErrorMsg(List<String> placeHolderExpressions) {
        String msg = "No value provided for placeholder expressions: ";
        for(String placeHolderExpression:placeHolderExpressions) {
            msg = msg + placeholderPrefix + placeHolderExpression + placeholderSuffix + ", ";
        }
        return msg.replaceAll(", $","") + ".  Check your configuration!";
    }
}
