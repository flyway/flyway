/*-
 * ========================LICENSE_START=================================
 * flyway-core-utilities
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
package org.flywaydb.core.utilities.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * Loads TOML config files for the Maven and Gradle plugins.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BuildToolTomlConfiguration {

    public static final String ENVIRONMENT = "flyway.environment";
    public static final String ENVIRONMENT_VARIABLE = "FLYWAY_ENVIRONMENT";
    private static final String DEFAULT_ENVIRONMENT = "default";

    /**
     * Builds the plugin configuration from TOML config files, keeping the precedence of their legacy config files:
     * build tool settings, then the TOML files, then environment variables and system properties. The same precedence
     * picks the environment from {@code flyway.environment}. Relative filesystem locations in the TOML files are
     * resolved against the working directory.
     */
    public static ClassicConfiguration load(final List<File> tomlFiles,
        final File workingDirectory,
        final ClassLoader classLoader,
        final Map<String, String> buildToolSettings,
        final Map<String, String> overrides) {
        final ConfigurationModel toml = TomlUtils.loadConfigurationFiles(tomlFiles);
        makeRelativeLocationsBasedOnWorkingDirectory(toml, workingDirectory.getAbsolutePath());

        final Map<String, String> buildToolSettingsWithoutEnvironment = new HashMap<>(buildToolSettings);
        final Map<String, String> overridesWithoutEnvironment = new HashMap<>(overrides);
        final String environment = determineEnvironment(buildToolSettingsWithoutEnvironment.remove(ENVIRONMENT),
            toml.getFlyway().getEnvironment(),
            overridesWithoutEnvironment.remove(ENVIRONMENT));
        if (!DEFAULT_ENVIRONMENT.equals(environment) && !toml.getEnvironments().containsKey(environment)) {
            throw new FlywayException("Environment '"
                + environment
                + "' not found. Check that this environment exists in your configuration.");
        }

        final ConfigurationModel buildToolModel = ConfigurationModel.defaults();
        buildToolModel.getFlyway().setEnvironment(environment);
        buildToolModel.getEnvironments().putIfAbsent(environment, new EnvironmentModel());
        final ClassicConfiguration buildToolConfiguration = new ClassicConfiguration(buildToolModel);
        buildToolConfiguration.setClassLoader(classLoader);
        buildToolConfiguration.configure(buildToolSettingsWithoutEnvironment);

        final ConfigurationModel merged = buildToolConfiguration.getModernConfig().merge(toml);
        merged.getFlyway().setEnvironment(environment);

        final ClassicConfiguration configuration = new ClassicConfiguration(merged);
        configuration.setClassLoader(classLoader);
        configuration.setPluginRegister(buildToolConfiguration.getPluginRegister());
        ConfigurationExtensionBinder.bind(toml, environment, configuration.getPluginRegister());
        configuration.configure(overridesWithoutEnvironment);
        ConfigUtils.dumpConfigurationMap(ConfigUtils.getConfigurationMapFromModel(configuration.getModernConfig()),
            "Using configuration:");
        return configuration;
    }

    private static String determineEnvironment(final String buildToolEnvironment,
        final String tomlEnvironment,
        final String overrideEnvironment) {
        if (StringUtils.hasText(overrideEnvironment)) {
            return overrideEnvironment;
        }
        if (StringUtils.hasText(tomlEnvironment)) {
            return tomlEnvironment;
        }
        if (StringUtils.hasText(buildToolEnvironment)) {
            return buildToolEnvironment;
        }
        return DEFAULT_ENVIRONMENT;
    }

    private static void makeRelativeLocationsBasedOnWorkingDirectory(final ConfigurationModel config,
        final String workingDirectory) {
        if (config.getFlyway().getLocations() != null) {
            ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getLocations());
        }
        if (config.getFlyway().getCallbackLocations() != null) {
            ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory,
                config.getFlyway().getCallbackLocations());
        }
        ConfigUtils.makeRelativeLocationsInEnvironmentsBasedOnWorkingDirectory(workingDirectory, config.getEnvironments());
    }
}
