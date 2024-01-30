/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
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
import org.flywaydb.core.internal.configuration.resolvers.EnvironmentProvisioner;
import org.flywaydb.core.internal.configuration.resolvers.EnvironmentResolver;
import org.flywaydb.core.internal.configuration.resolvers.PropertyResolver;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_SQL_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory;

@CustomLog
public class ModernConfigurationManager implements ConfigurationManager {

    private static final Pattern ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN = Pattern.compile("\\[\"([^\"]*)\"]");
    private static final String UNABLE_TO_PARSE_FIELD = "Unable to parse '%s' in your TOML configuration file";

    public Configuration getConfiguration(CommandLineArguments commandLineArguments) {
        String workingDirectory =
            commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory()
                : ClassUtils.getInstallDir(Main.class);

        List<File> tomlFiles = ConfigUtils.getDefaultTomlConfigFileLocations(
            new File(ClassUtils.getInstallDir(Main.class)), commandLineArguments.getWorkingDirectoryOrNull());
        tomlFiles.addAll(commandLineArguments.getConfigFilePathsFromEnv(true));
        tomlFiles.addAll(commandLineArguments.getConfigFiles().stream().map(File::new)
            .toList());

        ConfigurationModel config = TomlUtils.loadConfigurationFiles(
            tomlFiles.stream().filter(File::exists).collect(Collectors.toList()));

        ConfigurationModel commandLineArgumentsModel = TomlUtils.loadConfigurationFromCommandlineArgs(
            commandLineArguments.getConfiguration(true));
        ConfigurationModel environmentVariablesModel = TomlUtils.loadConfigurationFromEnvironment();
        config = config.merge(environmentVariablesModel)
            .merge(commandLineArgumentsModel);

        if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME) ||
            environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
            EnvironmentModel defaultEnv = config.getEnvironments().get(config.getFlyway().getEnvironment());

            if (environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
                EnvironmentModel environmentVariablesEnv = environmentVariablesModel.getEnvironments()
                    .get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
                EnvironmentModel mergedModel =
                    defaultEnv == null ? environmentVariablesEnv : defaultEnv.merge(environmentVariablesEnv);
                config.getEnvironments().put(config.getFlyway().getEnvironment(), mergedModel);
            }

            if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
                EnvironmentModel commandLineArgumentsEnv = commandLineArgumentsModel.getEnvironments()
                    .get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
                EnvironmentModel mergedModel =
                    defaultEnv == null ? commandLineArgumentsEnv : defaultEnv.merge(commandLineArgumentsEnv);
                config.getEnvironments().put(config.getFlyway().getEnvironment(), mergedModel);
            }

            config.getEnvironments().remove(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
        }

        Map<String, Map<String, String>> envConfigs = commandLineArguments.getEnvironmentConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String envKey : envConfigs.keySet()) {
            try {
                final Map<String, String> envValue = envConfigs.get(envKey);
                final Map<String, Object> envValueObject = new HashMap<>();
                envValue.entrySet().forEach(entry -> {
                    if(entry.getKey().startsWith("jdbcProperties.")) {
                        envValueObject.computeIfAbsent("jdbcProperties", s -> new HashMap<String, String>());
                        ((Map<String, String>)envValueObject.get("jdbcProperties")).put(entry.getKey().substring("jdbcProperties.".length()), entry.getValue());
                    } else {
                        envValueObject.put(entry.getKey(), entry.getValue());
                    }
                });
                EnvironmentModel env = objectMapper.convertValue(envValueObject, EnvironmentModel.class);

                if (config.getEnvironments().containsKey(envKey)) {
                    env = config.getEnvironments().get(envKey).merge(env);
                }
                config.getEnvironments().put(envKey, env);

            } catch (IllegalArgumentException exc) {
                String fieldName = exc.getMessage().split("\"")[1];
                throw new FlywayException(
                    String.format("Failed to configure parameter: '%s' in your '%s' environment", fieldName, envKey));
            }


        }

        File sqlFolder = new File(workingDirectory, DEFAULT_CLI_SQL_LOCATION);
        if (ConfigUtils.shouldUseDefaultCliSqlLocation(sqlFolder,
            !config.getFlyway().getLocations().equals(ConfigurationModel.defaults().getFlyway().getLocations()))) {
            config.getFlyway().setLocations(Arrays.stream(new String[]{"filesystem:" + sqlFolder.getAbsolutePath()}).collect(Collectors.toList()));
        }

        if (commandLineArguments.isWorkingDirectorySet()) {
            makeRelativeLocationsBasedOnWorkingDirectory(commandLineArguments.getWorkingDirectory(),
                config.getFlyway().getLocations());
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
        for (ConfigurationExtension configurationExtension : cfg.getPluginRegister()
            .getPlugins(ConfigurationExtension.class)) {
            if (configurationExtension.getNamespace().isEmpty()) {
                processParametersByNamespace("plugins", config, configurationExtension, configuredPluginParameters);
            }
            processParametersByNamespace(configurationExtension.getNamespace(), config, configurationExtension,
                configuredPluginParameters);
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

        List<String> missingParams = pluginParametersWhichShouldHaveBeenConfigured.stream()
            .filter(p -> !configuredPluginParameters.contains(p))
            .toList();

        if (!missingParams.isEmpty()) {
            throw new FlywayException(
                "Failed to configure Parameters: " + String.join(", ", missingParams));
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

    private void processParametersByNamespace(String namespace, ConfigurationModel config,
        ConfigurationExtension configurationExtension,
        List<String> configuredPluginParameters) {
        Map<String, Object> pluginConfigs = config.getFlyway().getPluginConfigurations();

        boolean suppressError = false;

        if (namespace.startsWith("\\")) {
            suppressError = true;
            namespace = namespace.substring(1);
            pluginConfigs = config.getRootConfigurations();
        }
        if (pluginConfigs.containsKey(namespace) || namespace.isEmpty()) {
            List<String> fields = Arrays.stream(configurationExtension.getClass().getDeclaredFields())
                .map(Field::getName)
                .toList();
            Map<String, Object> values =
                !namespace.isEmpty() ? (Map<String, Object>) pluginConfigs.get(namespace) : pluginConfigs;

            values = values
                .entrySet()
                .stream()
                .filter(p -> fields.stream().anyMatch(k -> k.equalsIgnoreCase(p.getKey())))
                .collect(Collectors.toMap(
                    p -> fields.stream()
                        .filter(q -> q.equalsIgnoreCase(p.getKey()))
                        .findFirst()
                        .orElse(p.getKey()),
                    Map.Entry::getValue));

            try {
                if (configurationExtension.isStub() && new HashSet<>(configuredPluginParameters).containsAll(
                    values.keySet())) {
                    return;
                }

                final Map<String, Object> finalValues = values;
                Arrays.stream(configurationExtension.getClass().getDeclaredFields())
                    .filter(f -> List.of(List.class, String[].class).contains(f.getType()))
                    .forEach(f -> {
                        String fieldName = f.getName();
                        Object fieldValue = finalValues.get(fieldName);
                        if (fieldValue instanceof String) {
                            finalValues.put(fieldName, fieldValue.toString().split(","));
                        }
                    });

                ObjectMapper mapper = new ObjectMapper();
                if (suppressError) {
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                }

                ConfigurationExtension newConfigurationExtension = mapper.convertValue(finalValues,
                    configurationExtension.getClass());
                if (suppressError) {
                    try {
                        ConfigurationExtension dummyConfigurationExtension = (new ObjectMapper()).convertValue(
                            finalValues, configurationExtension.getClass());
                    } catch (final IllegalArgumentException e) {
                        final var fullFieldName = getFullFieldNameFromException(namespace, e);

                        LOG.warn(String.format(UNABLE_TO_PARSE_FIELD, fullFieldName));
                    }
                }
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
            } catch (final IllegalArgumentException e) {
                final var fullFieldName = getFullFieldNameFromException(namespace, e);
                if (suppressError) {
                    LOG.warn(String.format(UNABLE_TO_PARSE_FIELD, fullFieldName));
                } else {
                    LOG.error(String.format(UNABLE_TO_PARSE_FIELD, fullFieldName));
                }
            }
        }
    }

    private static String getFullFieldNameFromException(final String namespace, final IllegalArgumentException e) {
        final var matcher = ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN.matcher(e.getMessage());
        final var fullFieldName = new StringBuilder();
        if (!namespace.isEmpty()) {
            fullFieldName.append(namespace);
        }

        while (matcher.find()) {
            if (!fullFieldName.isEmpty()) {
                fullFieldName.append(".");
            }
            fullFieldName.append(matcher.group(1));
        }
        return fullFieldName.toString();
    }
}