/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.configuration.resolvers;

import static org.flywaydb.core.internal.configuration.resolvers.ProvisionerConfiguration.createConfigurationWithEnvironment;

import java.util.Collection;
import java.util.Optional;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;

public class PropertyResolverContextImpl implements PropertyResolverContext {

    private final Map<String, PropertyResolver> resolvers;
    private final Map<String, ConfigurationExtension> resolverConfigurations;
    private final String environmentName;
    private final Configuration configuration;

    private static final CharsetEncoder ASCII_ENCODER = StandardCharsets.US_ASCII.newEncoder();
    private static final Pattern NESTED_RESOLVER_PATTERN = Pattern.compile("(^|[^$])\\$\\{(([^}]+|)\\$\\{.+?}).*?}");
    private static final Pattern RESOLVER_REGEX_PATTERN = Pattern.compile("\\${1,2}\\{[^.]+\\.[^.]+?\\}");
    private static final Pattern VERBATIM_REGEX_PATTERN = Pattern.compile("\\!\\{.*\\}");

    public PropertyResolverContextImpl(final String environmentName,
        final Configuration configuration,
        final Map<String, PropertyResolver> resolvers,
        final Map<String, ConfigurationExtension> resolverConfigurations) {
        this.environmentName = environmentName;
        this.configuration = createConfigurationCopy(configuration, environmentName);
        this.resolvers = resolvers;
        this.resolverConfigurations = Optional.ofNullable(resolverConfigurations).orElseGet(Map::of);
    }

    @Override
    public ConfigurationExtension getResolverConfiguration(final String resolverName) {
        return resolverConfigurations.get(resolverName);
    }

    @Override
    public ConfigurationExtension getResolverConfigurationOrThrow(final String resolverName) {
        return Optional.ofNullable(getResolverConfiguration(resolverName))
            .orElseThrow(() -> new FlywayException("Required configuration not defined for resolver/provisioner \""
                + resolverName
                + "\" for environment "
                + environmentName, CoreErrorCode.CONFIGURATION));
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public FlywayTelemetryManager getTelemetryManager() {
        return configuration.getPluginRegister().getInstanceOf(FlywayTelemetryManager.class);
    }

    @Override
    public String getWorkingDirectory() {
        final var workingDirectory = configuration.getWorkingDirectory();
        if(workingDirectory == null) {
            return System.getProperty("user.dir");
        } else {
            return workingDirectory;
        }
    }

    @Override
    public String getEnvironmentName() {
        return environmentName;
    }

    @Override
    public String resolveValue(final String value, final ProgressLogger progress) {
        if (value == null) {
            return null;
        }
        if (isVerbatim(value)) {
            return value.substring(2, value.length() - 1);
        }
        if (hasNestedResolvers(value)) {
            throw new FlywayException("Resolvers cannot be nested: " + value, CoreErrorCode.CONFIGURATION);
        }
        return RESOLVER_REGEX_PATTERN.matcher(value.strip()).replaceAll(m -> getPropertyResolverReplacement(m, progress));
    }

    @Override
    public String resolveValueOrThrow(final String input, final ProgressLogger progress, final String propertyName) {
        final var result = resolveValue(input, progress);
        if (result == null) {
            throw new FlywayException("Configuration value " + propertyName + " not specified for environment " + environmentName, CoreErrorCode.CONFIGURATION);
        }
        return result;
    }

    @Override
    public Collection<String> resolveValues(final Collection<String> input, final ProgressLogger progress) {
        if (input == null) {
            return null;
        }
        return input.stream().map(v -> resolveValue(v, progress)).toList();
    }

    private boolean isVerbatim(final String value) {
        return VERBATIM_REGEX_PATTERN.matcher(value.strip()).matches();
    }

    private String getPropertyResolverReplacement(final MatchResult resolverMatchResult, final ProgressLogger progress) {
        // '\' are ignored by Matcher and '$' will break it so both need escaping with '\'.
        // See https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html#replaceAll-java.lang.String-
        return parsePropertyResolver(resolverMatchResult, progress)
            .replaceAll("\\\\", "\\\\\\\\")
            .replaceAll("\\$", "\\\\\\$");
    }

    private String parsePropertyResolver(final MatchResult resolverMatchResult, final ProgressLogger progress) {
        final String resolverMatch = resolverMatchResult.group();

        if (resolverMatch.startsWith("$$")) {
            return resolverMatch.substring(1);
        }

        final String resolverName = resolverMatch.substring(2, resolverMatch.indexOf(".")).strip();
        if (!resolvers.containsKey(resolverName)) {
            throw new FlywayException("Unknown resolver '" + resolverName + "' for environment " + environmentName, CoreErrorCode.CONFIGURATION);
        }

        final String resolverParam;
        if (resolverMatch.contains(":")) {
            resolverParam = resolverMatch.substring(resolverMatch.indexOf(".") + 1, resolverMatch.indexOf(":")).strip();
            final String filter = resolverMatch.substring(resolverMatch.indexOf(":") + 1, resolverMatch.length() - 1).strip();
            return filter(resolvers.get(resolverName).resolve(resolverParam, this, progress), filter);
        }

        resolverParam = resolverMatch.substring(resolverMatch.indexOf(".") + 1, resolverMatch.length() - 1).strip();
        return resolvers.get(resolverName).resolve(resolverParam, this, progress);
    }

    static String filter(final String str, final String filter) {
        return str.chars().filter(c -> isAllowed((char) c, filter))
                  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                  .toString();
    }

    private static boolean isAllowed(final char c, final String filter) {
        return (filter.contains("D") && Character.isDigit(c)) ||
                (filter.contains("A") && Character.isLetter(c)) ||
                (filter.contains("a") && Character.isLetter(c) && ASCII_ENCODER.canEncode(c)) ||
                (filter.contains("d") && Character.isDigit(c) && ASCII_ENCODER.canEncode(c));
    }

    private static boolean hasNestedResolvers(final String value) {
        final var matcher = NESTED_RESOLVER_PATTERN.matcher(value);
        while (matcher.find()) {
            if (RESOLVER_REGEX_PATTERN.matcher(matcher.group(2)).find()) {
                return true;
            }
        }
        return false;
    }

    private static Configuration createConfigurationCopy(final Configuration configuration,
        final String environmentName) {
        final var environmentModel = Optional.ofNullable(configuration)
            .map(Configuration::getModernConfig)
            .map(ConfigurationModel::getEnvironments)
            .map(envs -> envs.get(environmentName))
            .orElseThrow(() -> new FlywayException("Unable to provision environment "
                + environmentName
                + " as required configuration is not defined", CoreErrorCode.CONFIGURATION));

        return createConfigurationWithEnvironment(configuration, environmentName, environmentModel);
    }
}
