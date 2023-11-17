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
package org.flywaydb.commandline.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.TomlUtils;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_SQL_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory;

@CustomLog
public class ModernConfigurationManager implements ConfigurationManager {

    private static final Pattern ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN = Pattern.compile("\"([^\"]*)\"");

    public Configuration getConfiguration(CommandLineArguments commandLineArguments) {
        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : ClassUtils.getInstallDir(Main.class);

        List<File> tomlFiles = ConfigUtils.getDefaultTomlConfigFileLocations(new File(ClassUtils.getInstallDir(Main.class)));
        tomlFiles.addAll(CommandLineConfigurationUtils.getTomlConfigFilePaths());
        tomlFiles.addAll(commandLineArguments.getConfigFiles().stream().map(File::new)
                                             .collect(Collectors.toList()));

        List<File> existingFiles = tomlFiles.stream().filter(File::exists).collect(Collectors.toList());
        ConfigurationModel config = TomlUtils.loadConfigurationFiles(existingFiles, workingDirectory);

        ConfigurationModel commandLineArgumentsModel = TomlUtils.loadConfigurationFromCommandlineArgs(commandLineArguments.getConfiguration(true));
        ConfigurationModel environmentVariablesModel = TomlUtils.loadConfigurationFromEnvironment();
        config = config.merge(environmentVariablesModel)
                       .merge(commandLineArgumentsModel);

        if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME) ||
                environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
            EnvironmentModel defaultEnv = config.getEnvironments().get(config.getFlyway().getEnvironment());

            if (environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
                config.getEnvironments().put(config.getFlyway().getEnvironment(), defaultEnv.merge(environmentVariablesModel.getEnvironments().get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)));
            }

            if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
                config.getEnvironments().put(config.getFlyway().getEnvironment(), defaultEnv.merge(commandLineArgumentsModel.getEnvironments().get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)));
            }

            config.getEnvironments().remove(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
        }

        File sqlFolder = new File(workingDirectory, DEFAULT_CLI_SQL_LOCATION);
        if (ConfigUtils.shouldUseDefaultCliSqlLocation(sqlFolder, !config.getFlyway().getLocations().equals(ConfigurationModel.defaults().getFlyway().getLocations()))) {
            config.getFlyway().setLocations(Arrays.asList("filesystem:" + sqlFolder.getAbsolutePath()));
        }

        if (commandLineArguments.isWorkingDirectorySet()) {
            makeRelativeLocationsBasedOnWorkingDirectory(commandLineArguments.getWorkingDirectory(), config.getFlyway().getLocations());
        }

        ConfigUtils.dumpConfigurationModel(config);
        ClassicConfiguration cfg = new ClassicConfiguration(config);

        cfg.setWorkingDirectory(workingDirectory);

        configurePlugins(config, cfg);

        loadJarDirsAndAddToClasspath(workingDirectory, cfg);

        return cfg;
    }

    private void configurePlugins(ConfigurationModel config, ClassicConfiguration cfg) {
        List<String> configuredPluginParameters = new ArrayList<>();
        for (ConfigurationExtension configurationExtension : cfg.getPluginRegister().getPlugins(ConfigurationExtension.class)) {
            if(configurationExtension.getNamespace().isEmpty()) {
                processParametersByNamespace("plugins", config, configurationExtension, configuredPluginParameters);
            }
            processParametersByNamespace(configurationExtension.getNamespace(), config, configurationExtension, configuredPluginParameters);
        }
        Map<String, Object> pluginConfigurations = config.getFlyway().getPluginConfigurations();
        pluginConfigurations.remove("jarDirs");

        List<String> pluginParametersWhichShouldHaveBeenConfigured = new ArrayList<>();
        for (Map.Entry<String, Object> configuration : pluginConfigurations.entrySet()) {
            if (configuration.getValue() instanceof Map<?, ?>) {
                Map<String, Object> temp = (Map<String, Object>) configuration.getValue();
                pluginParametersWhichShouldHaveBeenConfigured.addAll(temp.keySet());
            } else {
                pluginParametersWhichShouldHaveBeenConfigured.add(configuration.getKey());
            }
        }

        List<String> missingParams = pluginParametersWhichShouldHaveBeenConfigured.stream().filter(p -> !configuredPluginParameters.contains(p)).toList();

        if (!missingParams.isEmpty()) {
            throw new FlywayException("Failed to configure Parameters: " + missingParams.stream().collect(Collectors.joining(", ")));
        }
    }

    private static void loadJarDirsAndAddToClasspath(String workingDirectory, ClassicConfiguration cfg) {
        List<String> jarDirs = new ArrayList<>();

        File jarDir = new File(workingDirectory, "jars");
        ConfigUtils.warnIfUsingDeprecatedMigrationsFolder(jarDir, ".jar");
        if (jarDir.exists()) {
            jarDirs.add(jarDir.getAbsolutePath());
        }

        ResolvedEnvironment resolvedEnvironment = cfg.getCurrentResolvedEnvironment();
        if (resolvedEnvironment != null) {
            jarDirs.addAll(resolvedEnvironment.getJarDirs());
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<File> jarFiles = new ArrayList<>();
        jarFiles.addAll(CommandLineConfigurationUtils.getJdbcDriverJarFiles());
        jarFiles.addAll(CommandLineConfigurationUtils.getJavaMigrationJarFiles(jarDirs.toArray(new String[0])));

        if (!jarFiles.isEmpty()) {
            classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
        }

        cfg.setClassLoader(classLoader);
    }

    private void processParametersByNamespace(String namespace, ConfigurationModel config, ConfigurationExtension configurationExtension,
                                              List<String> configuredPluginParameters) {
        Map<String, Object> pluginConfigs = config.getFlyway().getPluginConfigurations();

        boolean suppressError = false;

        if (namespace.startsWith("\\")) {
            suppressError = true;
            namespace = namespace.substring(1);
            pluginConfigs = config.getRootConfigurations();
        }
        if (pluginConfigs.containsKey(namespace) || namespace.isEmpty()) {
            List<String> fields = Arrays.stream(configurationExtension.getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
            Map<String, Object> values = !namespace.isEmpty() ? (Map<String, Object>) pluginConfigs.get(namespace) : pluginConfigs;

            values = values
                    .entrySet()
                    .stream()
                    .filter(p -> fields.stream().anyMatch(k -> k.equalsIgnoreCase(p.getKey())))
                    .collect(Collectors.toMap(
                            p-> fields.stream()
                                      .filter(q->q.equalsIgnoreCase(p.getKey()))
                                      .findFirst()
                                      .orElse(p.getKey()),
                            Map.Entry::getValue));

            try {
                if (configurationExtension.isStub() && new HashSet<>(configuredPluginParameters).containsAll(values.keySet())) {
                    return;
                }
                ConfigurationExtension newConfigurationExtension = new ObjectMapper().convertValue(values, configurationExtension.getClass());
                MergeUtils.mergeModel(newConfigurationExtension, configurationExtension);

                if (!values.isEmpty()) {
                    for (Map.Entry<String, Object> entry : values.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?> && namespace.isEmpty()) {
                            Map<String, Object> temp = (Map<String, Object>) entry.getValue();
                            configuredPluginParameters.addAll(temp.keySet());
                        } else {
                            configuredPluginParameters.add(entry.getKey());
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                Matcher matcher = ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN.matcher(e.getMessage());
                if (matcher.find()) {
                    if (suppressError) {
                        LOG.warn("Unable to parse the field: " + matcher.group(1));
                    } else {
                        LOG.error("Unable to parse the field: " + matcher.group(1));
                    }
                }
            }
        }
    }
}