/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.commandline.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import lombok.CustomLog;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.TomlUtils;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.FlywayEnvironmentModel;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.MergeUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.flywaydb.core.internal.util.StringUtils;

import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_JARS_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_SQL_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.dumpEnvironmentModel;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeJarDirsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeJarDirsInEnvironmentsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsInEnvironmentsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.util.ExceptionUtils.getFlywayExceptionMessage;

@CustomLog
public class ModernConfigurationManager implements ConfigurationManager {

    private static final Pattern ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN = Pattern.compile("\\[\"([^\"]*)\"]");
    private static final String UNABLE_TO_PARSE_FIELD = "Unable to parse parameter '%s'.";
    private static final String FLYWAY_NAMESPACE = "flyway";

    public Configuration getConfiguration(CommandLineArguments commandLineArguments) {
        String installDirectory =
            commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory()
                : ClassUtils.getInstallDir(Main.class);
        String workingDirectory = commandLineArguments.getWorkingDirectoryOrNull();

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

        if (ConfigUtils.detectNullConfigModel(environmentVariablesModel)) {
            LOG.debug("Skipping empty environment variables");
        } else {
            ConfigUtils.dumpConfigurationModel(environmentVariablesModel, "Loading configuration from environment variables:");
            config = config.merge(environmentVariablesModel);
        }

        if (ConfigUtils.detectNullConfigModel(commandLineArgumentsModel)) {
            LOG.debug("No flyway namespace variables found in command line");
        } else {
            ConfigUtils.dumpConfigurationModel(commandLineArgumentsModel, "Loading configuration from command line arguments:");
            config = config.merge(commandLineArgumentsModel);
        }


        if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME) ||
            environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
            EnvironmentModel defaultEnv = config.getEnvironments().get(config.getFlyway().getEnvironment());
            EnvironmentModel mergedModel = null;

            if (environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
                EnvironmentModel environmentVariablesEnv = environmentVariablesModel.getEnvironments()
                    .get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
                mergedModel =
                    defaultEnv == null ? environmentVariablesEnv : defaultEnv.merge(environmentVariablesEnv);
            }

            if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
                EnvironmentModel commandLineArgumentsEnv = commandLineArgumentsModel.getEnvironments()
                    .get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
                mergedModel = mergedModel == null ?
                    defaultEnv == null ? commandLineArgumentsEnv : defaultEnv.merge(commandLineArgumentsEnv) :
                    mergedModel.merge(commandLineArgumentsEnv);
            }

            if (mergedModel != null) {
                LOG.debug("Merged " + ClassicConfiguration.TEMP_ENVIRONMENT_NAME + " into the " + config.getFlyway().getEnvironment() + " environment");
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
                final Map<String, String> flywayEnvironmentModelArguments = new HashMap<>();

                envValue.entrySet().forEach(entry -> {
                    if(entry.getKey().startsWith("jdbcProperties.")) {
                        envValueObject.computeIfAbsent("jdbcProperties", s -> new HashMap<String, String>());
                        ((Map<String, String>)envValueObject.get("jdbcProperties")).put(entry.getKey().substring("jdbcProperties.".length()), entry.getValue());
                    } else if (entry.getKey().startsWith("flyway.")) {
                        flywayEnvironmentModelArguments.put(entry.getKey(), entry.getValue());
                    } else if (entry.getKey().equals("schemas")) {
                        envValueObject.put(entry.getKey(), Arrays.stream(entry.getValue().split(",")).map(String::trim).toList());
                    } else if (entry.getKey().startsWith("resolvers.")) {
                        handleResolverCommandLineArgs(envKey, entry, envValueObject);
                    } else {
                        envValueObject.put(entry.getKey(), entry.getValue());
                    }
                });

                envValueObject.put(FLYWAY_NAMESPACE,
                    new FlywayEnvironmentModel().merge(TomlUtils.loadConfigurationFromCommandlineArgs(
                        flywayEnvironmentModelArguments).getFlyway()));

                EnvironmentModel env = objectMapper.convertValue(envValueObject, EnvironmentModel.class);
                dumpEnvironmentModel(env, envKey, "Loading environment configuration from command line:");

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

        if (workingDirectory != null) {
            makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getLocations());
            makeRelativeLocationsInEnvironmentsBasedOnWorkingDirectory(workingDirectory, config.getEnvironments());
            makeRelativeJarDirsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getJarDirs());
            makeRelativeJarDirsInEnvironmentsBasedOnWorkingDirectory(workingDirectory, config.getEnvironments());
        }

        ConfigUtils.dumpConfigurationModel(config, "Using configuration:");
        ClassicConfiguration cfg = new ClassicConfiguration(config);

        cfg.setWorkingDirectory(workingDirectory);

        configurePlugins(config, cfg);

        loadJarDirsAndAddToClasspath(installDirectory, cfg);

        setDefaultSqlLocation(installDirectory, cfg);

        return cfg;
    }

    private static void handleResolverCommandLineArgs(final String environment,
        final Entry<String, String> resolverEntry,
        final Map<? super String, Object> envValueObject) {

        final var resolverParts = resolverEntry.getKey().split("\\.");
        // resolvers.<resolverName>.<resolverProperty> = <resolverValue>
        if (resolverParts.length == 3) {
            final var resolvers = (Map<String, Map<String, Object>>) envValueObject.computeIfAbsent(resolverParts[0],
                s -> new HashMap<String, Map<String, Object>>());
            final var resolver = resolvers.computeIfAbsent(resolverParts[1], s -> new HashMap<>());
            resolver.put(resolverParts[2], resolverEntry.getValue());
        } else {
            throw new FlywayException(
                String.format("Invalid resolver configuration for environment %s: %s", environment, resolverEntry.getKey()));
        }
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

        boolean rootConfigurationsIsEmpty = config.getRootConfigurations().isEmpty();

        final List<FlywayException> configurationExceptions = new ArrayList<>();

        try {
            checkUnknownParamsInFlywayNamespace(config.getFlyway(),
                configuredPluginParameters, rootConfigurationsIsEmpty,
                "flyway.");
        } catch (FlywayException e) {
            configurationExceptions.add(e);
        }
        try {
            checkUnknownParamsInFlywayNamespace(config.getEnvironments().get(cfg.getCurrentEnvironmentName()).getFlyway(),
                Collections.emptyList(),
                rootConfigurationsIsEmpty,
                "environments."+cfg.getCurrentEnvironmentName()+".flyway.");
        } catch (final FlywayException e) {
            configurationExceptions.add(e);
        }

        if(!configurationExceptions.isEmpty()) {
            combineConfigurationExceptions(configurationExceptions);
        }

    }

    private static void setDefaultSqlLocation(final String installDirectory, final ClassicConfiguration cfg) {
        File sqlFolder = new File(installDirectory, DEFAULT_CLI_SQL_LOCATION);
        Location[] defaultLocations = new Locations(ConfigurationModel.defaults()
            .getFlyway()
            .getLocations()
            .toArray(new String[0])).getLocations().toArray(new Location[0]);
        if (ConfigUtils.shouldUseDefaultCliSqlLocation(sqlFolder,
            !Arrays.equals(cfg.getLocations(), defaultLocations))) {
            cfg.setLocations(new Location("filesystem:" + sqlFolder.getAbsolutePath()));
        }
    }

    private static void loadJarDirsAndAddToClasspath(String workingDirectory, ClassicConfiguration cfg) {
        List<String> jarDirs = new ArrayList<>();

        File jarDir = new File(workingDirectory, DEFAULT_CLI_JARS_LOCATION);
        ConfigUtils.warnIfUsingDeprecatedMigrationsFolder(jarDir, ".jar");
        if (jarDir.exists()) {
            jarDirs.add(jarDir.getAbsolutePath());
        }

        jarDirs.addAll(cfg.getJarDirs());

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
                    .filter(f -> List.of(Collection.class, List.class, String[].class).contains(f.getType()))
                    .forEach(f -> {
                        String fieldName = f.getName();
                        Object fieldValue = finalValues.get(fieldName);
                        if (fieldValue instanceof final String fieldValueString) {
                            finalValues.put(fieldName,
                                StringUtils.hasText(fieldValueString) ? fieldValueString.split(",") : new String[0]);
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
                var message = String.format(UNABLE_TO_PARSE_FIELD, fullFieldName);
                message += getFlywayExceptionMessage(e).map(text -> " " + text).orElse("");

                if (suppressError) {
                    LOG.warn(message);
                } else {
                    LOG.error(message);
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

    private void checkUnknownParamsInFlywayNamespace(final FlywayEnvironmentModel flyway,
        final Collection<String> configuredPluginParameters,
        boolean rootConfigurationsIsEmpty,
        final String prefix) {
        final Map<String, Object> pluginConfigurations = flyway.getPluginConfigurations();

        final Map<String, List<String>> pluginParametersWhichShouldHaveBeenConfigured = getPluginParametersWhichShouldHaveBeenConfigured(pluginConfigurations);

        final Map<String, List<String>> missingParams = getUnrecognisedParameters(pluginParametersWhichShouldHaveBeenConfigured,
            configuredPluginParameters);

        if (!missingParams.isEmpty()) {
            throwMissingParameters(flyway, missingParams, rootConfigurationsIsEmpty, prefix);
        }
    }

    private static Map<String, List<String>> getUnrecognisedParameters(final Map<String,
        List<String>> pluginParametersWhichShouldHaveBeenConfigured,
        final Collection<String> configuredPluginParameters) {
        final Map<String, List<String>> missingParams = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : pluginParametersWhichShouldHaveBeenConfigured.entrySet()) {
            final List<String> missing = entry.getValue().stream()
                .filter(p -> !configuredPluginParameters.contains(p))
                .collect(Collectors.toList());
            if (!missing.isEmpty()) {
                missingParams.put(entry.getKey(), missing);
            }
        }
        return missingParams;
    }

    private Map<String, List<String>> getPluginParametersWhichShouldHaveBeenConfigured(final Map<String, Object> pluginConfigurations) {
        final Map<String, List<String>> pluginParametersWhichShouldHaveBeenConfigured = new HashMap<>();
        for (final Map.Entry<String, Object> configuration : pluginConfigurations.entrySet()) {
            if (configuration.getValue() instanceof final Map<?, ?> temp) {

                pluginParametersWhichShouldHaveBeenConfigured.put(configuration.getKey(), temp.keySet().stream().map(Object::toString).toList());
            } else {
                if (!pluginParametersWhichShouldHaveBeenConfigured.containsKey(FLYWAY_NAMESPACE)) {
                    pluginParametersWhichShouldHaveBeenConfigured.put(FLYWAY_NAMESPACE, new ArrayList<>());
                }
                pluginParametersWhichShouldHaveBeenConfigured.get(FLYWAY_NAMESPACE).add(configuration.getKey());
            }
        }
        return pluginParametersWhichShouldHaveBeenConfigured;
    }

    private static void throwMissingParameters(final FlywayEnvironmentModel model,
        final Map<String, ? extends List<String>> missingParams,
        boolean rootConfigurationsIsEmpty,
        final String prefix ) {

        if (rootConfigurationsIsEmpty) {

        final StringBuilder exceptionMessage = new StringBuilder();
        if(missingParams.containsKey(FLYWAY_NAMESPACE)) {
            final Map<String, List<String>> possibleConfiguration = missingParams.get(FLYWAY_NAMESPACE).stream()
                .collect(Collectors.toMap(p -> p, p -> ConfigUtils.getPossibleFlywayConfigurations(p, model)));
            for (final Map.Entry<String, List<String>> entry : possibleConfiguration.entrySet()) {
                exceptionMessage.append("\t")
                    .append("Parameter: ")
                    .append(prefix)
                    .append(entry.getKey())
                    .append("\n");
                if (!entry.getValue().isEmpty()) {
                    exceptionMessage.append("\t\t").append("Possible values:").append("\n");
                    entry.getValue().forEach(v -> exceptionMessage.append("\t\t").append("- ").append(prefix).append(v).append("\n"));
                }
            }
        }
        missingParams.entrySet().stream()
            .filter(e -> !e.getKey().equals(FLYWAY_NAMESPACE))
            .forEach(e ->
                e.getValue().forEach(p -> {
                    exceptionMessage.append("\t")
                        .append("Parameter:")
                        .append(prefix)
                        .append(e.getKey())
                        .append(".")
                        .append(p)
                        .append("\n");
                })
            );


        exceptionMessage.deleteCharAt(exceptionMessage.length() - 1);
        throw new FlywayException(exceptionMessage.toString());

        }

    }

    private static void combineConfigurationExceptions(final Iterable<? extends FlywayException> configurationExceptions) {
        final StringBuilder exceptionMessage = new StringBuilder("Failed to configure parameters:").append("\n");
        configurationExceptions.forEach(e -> exceptionMessage.append(e.getMessage()).append("\n"));
        exceptionMessage.deleteCharAt(exceptionMessage.length() - 1);
        final FlywayException flywayException= new FlywayException(exceptionMessage.toString());
        configurationExceptions.forEach(flywayException::addSuppressed);
        throw flywayException;
    }
}
