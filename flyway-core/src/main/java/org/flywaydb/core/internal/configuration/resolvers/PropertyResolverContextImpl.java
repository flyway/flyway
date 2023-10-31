/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class PropertyResolverContextImpl implements PropertyResolverContext {

    private final Map<String, PropertyResolver> resolvers;
    private final Map<String, Map<String, Object>> resolverProperties;
    private final String environmentName;

    private static final CharsetEncoder ASCII_ENCODER = StandardCharsets.US_ASCII.newEncoder();
    private static final Pattern RESOLVER_REGEX_PATTERN = Pattern.compile("\\${1,2}\\{[^.]+\\.[^.]+\\}");
    private static final Pattern VERBATIM_REGEX_PATTERN = Pattern.compile("\\!\\{.*\\}");

    public PropertyResolverContextImpl(String environmentName, Map<String, PropertyResolver> resolvers, Map<String, Map<String, Object>> resolverProperties) {
        this.environmentName = environmentName;
        this.resolvers = resolvers;
        this.resolverProperties = resolverProperties;
    }

    public String resolvePropertyString(String resolverName, String propertyName) {
        if (resolverProperties == null) {
            return null;
        }

        Map<String, Object> properties = resolverProperties.get(resolverName);
        if (properties == null) {
            return null;
        }

        var propertyValue = properties.get(propertyName);
        if (!(propertyValue instanceof String)) {
            return null;
        }

        return resolveValue((String) propertyValue);
    }

    public List<String> resolvePropertyStringList(String resolverName, String propertyName) {
        if (resolverProperties == null) {
            return null;
        }

        Map<String, Object> properties = resolverProperties.get(resolverName);
        if (properties == null) {
            return null;
        }

        var propertyValue = properties.get(propertyName);
        if (!(propertyValue instanceof List)) {
            return null;
        }

        return ((List<?>) propertyValue).stream().filter(v -> v instanceof String).map(v -> resolveValue((String) v)).toList();
    }

    @Override
    public String getEnvironmentName() {
        return environmentName;
    }

    @Override
    public String resolveValue(String value) {
        if (value == null) {
            return null;
        }
        if (isVerbatim(value)) {
            return value.substring(2, value.length() - 1);
        }
        return RESOLVER_REGEX_PATTERN.matcher(value.strip()).replaceAll(this::parseResolverSyntax);
    }

    private boolean isVerbatim(String value) {
        return VERBATIM_REGEX_PATTERN.matcher(value.strip()).matches();
    }

    private String parseResolverSyntax(MatchResult resolverMatchResult) {
        String resolverMatch = resolverMatchResult.group();

        if (resolverMatch.startsWith("$$")) {
            //String containing '$' will break Matcher so '\' needed to escape it. See https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html#replaceAll-java.lang.String-
            return "\\" + resolverMatch.substring(1);
        }

        String resolverName = resolverMatch.substring(2, resolverMatch.indexOf(".")).strip();
        if (!resolvers.containsKey(resolverName)) {
            throw new FlywayException("Unknown resolver '" + resolverName + "' for environment " + environmentName, ErrorCode.CONFIGURATION);
        }

        String resolverParam;
        if (resolverMatch.contains(":")) {
            resolverParam = resolverMatch.substring(resolverMatch.indexOf(".") + 1, resolverMatch.indexOf(":")).strip();
            String filter = resolverMatch.substring(resolverMatch.indexOf(":") + 1, resolverMatch.length() - 1).strip();
            return filter(resolvers.get(resolverName).resolve(resolverParam, this), filter);
        }

        resolverParam = resolverMatch.substring(resolverMatch.indexOf(".") + 1, resolverMatch.length() - 1).strip();
        return resolvers.get(resolverName).resolve(resolverParam, this);
    }

    static String filter(String str, String filter) {
        return str.chars().filter(c -> isAllowed((char) c, filter))
                  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                  .toString();
    }

    private static boolean isAllowed(char c, String filter) {
        return (filter.contains("D") && Character.isDigit(c)) ||
                (filter.contains("A") && Character.isLetter(c)) ||
                (filter.contains("a") && Character.isLetter(c) && ASCII_ENCODER.canEncode(c)) ||
                (filter.contains("d") && Character.isDigit(c) && ASCII_ENCODER.canEncode(c));
    }
}