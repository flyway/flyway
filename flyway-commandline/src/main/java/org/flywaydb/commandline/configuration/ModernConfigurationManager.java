/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
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
package org.flywaydb.commandline.configuration;

import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_JARS_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_SQL_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.isOSS;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeJarDirsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeJarDirsInEnvironmentsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsInEnvironmentsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.warnForUnknownEnvParameters;
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.resolveUnknownParameter;

import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.configuration.ObsoleteParameter;
import org.flywaydb.core.api.exception.ObsoleteConfigurationParametersException;
import org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Kind;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.CustomLog;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.ProgressLoggerEmpty;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.utilities.configuration.ConfigurationExtensionBinder;
import org.flywaydb.core.utilities.configuration.TomlUtils;
import org.flywaydb.core.internal.configuration.Source;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.FlywayEnvironmentModel;
import org.flywaydb.core.internal.configuration.models.UnknownParameterModel;
import org.flywaydb.core.internal.configuration.resolvers.PropertyResolver;
import org.flywaydb.core.internal.configuration.resolvers.PropertyResolverContext;
import org.flywaydb.core.internal.configuration.resolvers.PropertyResolverContextImpl;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Locations;

@CustomLog
public class ModernConfigurationManager implements ConfigurationManager {

    private static final String FLYWAY_NAMESPACE = "flyway";

    public Configuration getConfiguration(final CommandLineArguments commandLineArguments) {
        final String installDirectory = commandLineArguments.isWorkingDirectorySet()
            ? commandLineArguments.getWorkingDirectory()
            : ClassUtils.getInstallDir(Main.class);
        final String workingDirectory = commandLineArguments.getWorkingDirectoryOrNull();

        final List<File> tomlFiles = ConfigUtils.getDefaultTomlConfigFileLocations(new File(ClassUtils.getInstallDir(
            Main.class)), commandLineArguments.getWorkingDirectoryOrNull());
        tomlFiles.addAll(commandLineArguments.getConfigFilePathsFromEnv(true));
        tomlFiles.addAll(commandLineArguments.getConfigFiles().stream().map(File::new).toList());

        ConfigurationModel config = ConfigurationModel.defaults();

        final Map<String, Source> sources = new HashMap<>();

        final Map<String, String> beforeToml = snapshot(config);
        config = config.merge(TomlUtils.loadConfigurationFiles(tomlFiles.stream()
            .filter(File::exists)
            .collect(Collectors.toList())));
        diffAndTag(sources, beforeToml, config, Source.TOML);

        final ConfigurationModel commandLineArgumentsModel = TomlUtils.loadConfigurationFromCommandlineArgs(
            commandLineArguments.getConfiguration(true));
        final ConfigurationModel environmentVariablesModel = TomlUtils.loadConfigurationFromEnvironment(config.getEnvironments()
            .keySet());

        if (ConfigUtils.detectNullConfigModel(environmentVariablesModel)) {
            LOG.debug("Skipping empty environment variables");
        } else {
            final Map<String, String> beforeEnvironmentVariables = snapshot(config);
            config = config.merge(environmentVariablesModel);
            diffAndTag(sources, beforeEnvironmentVariables, config, Source.ENVIRONMENT_VARIABLE);
        }

        if (ConfigUtils.detectNullConfigModel(commandLineArgumentsModel)) {
            LOG.debug("No flyway namespace variables found in command line");
        } else {
            final Map<String, String> beforeCommandLineArguments = snapshot(config);
            config = config.merge(commandLineArgumentsModel);
            diffAndTag(sources, beforeCommandLineArguments, config, Source.COMMAND_LINE);
        }

        if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)
            || environmentVariablesModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
            final String currentEnvironment = config.getFlyway().getEnvironment();
            boolean mergedTempEnvironment = false;

            final EnvironmentModel environmentVariablesEnv = environmentVariablesModel.getEnvironments()
                .get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
            if (environmentVariablesEnv != null) {
                final EnvironmentModel existingEnv = config.getEnvironments().get(currentEnvironment);
                final EnvironmentModel merged = existingEnv == null
                    ? environmentVariablesEnv
                    : existingEnv.merge(environmentVariablesEnv);

                final Map<String, String> beforeTempEnvironmentVariable = snapshot(config);
                config.getEnvironments().put(currentEnvironment, merged);
                diffAndTag(sources, beforeTempEnvironmentVariable, config, Source.ENVIRONMENT_VARIABLE);
                mergedTempEnvironment = true;
            }

            final EnvironmentModel commandLineArgumentsEnv = commandLineArgumentsModel.getEnvironments()
                .get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
            if (commandLineArgumentsEnv != null) {
                final EnvironmentModel existingEnv = config.getEnvironments().get(currentEnvironment);
                final EnvironmentModel merged = existingEnv == null
                    ? commandLineArgumentsEnv
                    : existingEnv.merge(commandLineArgumentsEnv);

                final Map<String, String> beforeTempCommandLineArguments = snapshot(config);
                config.getEnvironments().put(currentEnvironment, merged);
                diffAndTag(sources, beforeTempCommandLineArguments, config, Source.COMMAND_LINE);
                mergedTempEnvironment = true;
            }

            if (mergedTempEnvironment) {
                LOG.debug("Merged "
                    + ClassicConfiguration.TEMP_ENVIRONMENT_NAME
                    + " into the "
                    + currentEnvironment
                    + " environment");
            }

            config.getEnvironments().remove(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
        }

        final Map<String, Map<String, String>> envConfigs = commandLineArguments.getEnvironmentConfiguration();
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final String envKey : envConfigs.keySet()) {
            try {
                final Map<String, String> envValue = envConfigs.get(envKey);
                final Map<String, Object> envValueObject = new HashMap<>();
                final Map<String, String> flywayEnvironmentModelArguments = new HashMap<>();

                envValue.entrySet().forEach(entry -> {
                    if (entry.getKey().startsWith("jdbcProperties.")) {
                        envValueObject.computeIfAbsent("jdbcProperties", s -> new HashMap<String, String>());
                        ((Map<String, String>) envValueObject.get("jdbcProperties")).put(entry.getKey()
                            .substring("jdbcProperties.".length()), entry.getValue());
                    } else if (entry.getKey().startsWith("flyway.")) {
                        flywayEnvironmentModelArguments.put(entry.getKey(), entry.getValue());
                    } else if ("schemas".equals(entry.getKey())) {
                        envValueObject.put(entry.getKey(),
                            Arrays.stream(entry.getValue().split(",")).map(String::trim).toList());
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

                if (config.getEnvironments().containsKey(envKey)) {
                    env = config.getEnvironments().get(envKey).merge(env);
                }

                final Map<String, String> beforeNamedEnvironmentCommandLineArgs = snapshot(config);
                config.getEnvironments().put(envKey, env);
                diffAndTag(sources, beforeNamedEnvironmentCommandLineArgs, config, Source.COMMAND_LINE);
            } catch (final IllegalArgumentException exc) {
                final String fieldName = exc.getMessage().split("\"")[1];
                throw new FlywayException(String.format("Failed to configure parameter: '%s' in your '%s' environment",
                    fieldName,
                    envKey));
            }
        }

        warnForUnknownEnvParameters(config.getEnvironments());

        ConfigUtils.dumpConfigurationModel(config, "Using configuration:", sources);
        final ClassicConfiguration cfg = new ClassicConfiguration(config);

        cfg.setWorkingDirectory(workingDirectory);

        resolveConfigValues(config, cfg);

        makeRelativePathsBasedOnWorkingDirectory(workingDirectory, config);

        configurePlugins(config, cfg, commandLineArguments.shouldIgnoreUnrecognizedParameters());

        loadJarDirsAndAddToClasspath(installDirectory, cfg);

        if (!commandLineArguments.allOperationsSkipDefaultLocations()) {
            setDefaultSqlLocation(installDirectory, cfg);
        }

        return cfg;
    }

    /**
     * This is an experimental API and may be removed or changed in future versions.
     *
     * @param tomlFiles        list of configuration files to load
     * @param workingDirectory working directory to resolve relative paths in the configuration against
     * @return the loaded configuration
     */
    public Configuration getMcpActionConfiguration(final List<File> tomlFiles, final String workingDirectory) {
        final Map<String, Source> sources = new HashMap<>();

        ConfigurationModel config = ConfigurationModel.defaults();
        final Map<String, String> beforeToml = snapshot(config);
        config = config.merge(TomlUtils.loadConfigurationFiles(tomlFiles));
        diffAndTag(sources, beforeToml, config, Source.TOML);

        // Override properties which are not supported in MCP server tool configuration
        config.getFlyway().setOutputProgress(false);
        config.getFlyway().setOutputLogsInJson(false);
        config.getFlyway().setOutputType("json");
        config.getFlyway().setJarDirs(null);
        config.getFlyway().setLoggers(List.of());
        config.getEnvironments().forEach((key, model) -> {
            model.getFlyway().setJarDirs(null);
            model.getFlyway().setLoggers(List.of());
        });

        makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getLocations());
        makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getCallbackLocations());
        makeRelativeLocationsInEnvironmentsBasedOnWorkingDirectory(workingDirectory, config.getEnvironments());

        ConfigUtils.dumpConfigurationModel(config, "Using configuration:", sources);
        final ClassicConfiguration cfg = new ClassicConfiguration(config);
        cfg.setWorkingDirectory(workingDirectory);
        configurePlugins(config, cfg, false);
        setDefaultSqlLocation(workingDirectory, cfg);

        return cfg;
    }

    // Fields that EnvironmentModel eagerly initialises (schemas, jdbcProperties, flyway) render the same,
    // non-null value whether or not a stage actually configured them. Keyed by field name, not full dotted
    // key, so it applies regardless of which environment is being introduced.
    private static final Map<String, String> UNSET_ENVIRONMENT_FIELD_VALUES = unsetEnvironmentFieldValues();

    private static Map<String, String> unsetEnvironmentFieldValues() {
        final String envKey = "blank";
        final String prefix = "environments." + envKey + ".";
        return ConfigUtils.getEnvironmentMap(new EnvironmentModel(), envKey)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> entry.getKey().substring(prefix.length()), Map.Entry::getValue));
    }

    // Cheap snapshot for diffAndTag: skips the flattening work entirely when the configuration table won't be
    // logged, so source tracking costs nothing on the common path.
    private static Map<String, String> snapshot(final ConfigurationModel config) {
        return ConfigUtils.isConfigurationMapLoggingEnabled() ? ConfigUtils.getConfigurationMapFromModel(config) : Map.of();
    }

    static void diffAndTag(final Map<String, Source> sources,
        final Map<String, String> before,
        final ConfigurationModel after,
        final Source source) {
        if (!ConfigUtils.isConfigurationMapLoggingEnabled()) {
            return;
        }

        ConfigUtils.getConfigurationMapFromModel(after).forEach((key, value) -> {
            if (value.equals(before.get(key))) {
                return;
            }

            if (before.get(key) == null && isUnsetEnvironmentField(key, value)) {
                return;
            }
            sources.put(key, source);
        });
    }

    private static boolean isUnsetEnvironmentField(final String key, final String value) {
        if (!key.startsWith("environments.")) {
            return false;
        }
        final int fieldStart = key.indexOf('.', "environments.".length()) + 1;
        if (fieldStart == 0) {
            return false;
        }
        return value.equals(UNSET_ENVIRONMENT_FIELD_VALUES.get(key.substring(fieldStart)));
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
            throw new FlywayException(String.format("Invalid resolver configuration for environment %s: %s",
                environment,
                resolverEntry.getKey()));
        }
    }

    private static void resolveConfigValues(final ConfigurationModel config, final Configuration cfg) {
        final Map<String, PropertyResolver> resolvers = new HashMap<>();
        for (final PropertyResolver resolver : cfg.getPluginRegister().getInstancesOf(PropertyResolver.class)) {
            resolvers.put(resolver.getName(), resolver);
            for (final String alias : resolver.getAliases()) {
                resolvers.put(alias, resolver);
            }
        }

        final PropertyResolverContext context = new PropertyResolverContextImpl(cfg, resolvers);
        resolveStringValues(config.getFlyway().getPluginConfigurations(), context);
        resolveFlywayModelFields(config.getFlyway(), context);
    }

    @SuppressWarnings("unchecked")
    private static void resolveStringValues(final Map<String, Object> map, final PropertyResolverContext context) {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String value) {
                entry.setValue(context.resolveValue(value, new ProgressLoggerEmpty()));
            } else if (entry.getValue() instanceof Map) {
                resolveStringValues((Map<String, Object>) entry.getValue(), context);
            } else if (entry.getValue() instanceof List<?> list) {
                entry.setValue(list.stream()
                    .map(item -> item instanceof String s
                        ? (Object) context.resolveValue(s, new ProgressLoggerEmpty())
                        : item)
                    .toList());
            }
        }
    }

    private static void resolveFlywayModelFields(final Object model, final PropertyResolverContext context) {
        for (Class<?> clazz = model.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (final Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                    || field.getName().equals("pluginConfigurations")
                    || field.getName().equals("propertyResolvers")) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    final Object value = field.get(model);
                    if (value instanceof String s) {
                        field.set(model, context.resolveValue(s, new ProgressLoggerEmpty()));
                    } else if (value instanceof List<?> list) {
                        field.set(model,
                            new ArrayList<>(list.stream()
                                .map(item -> item instanceof String str ? (Object) context.resolveValue(str,
                                    new ProgressLoggerEmpty()) : item)
                                .toList()));
                    } else if (value instanceof Map<?, ?> map) {
                        final Map<String, Object> resolved = new HashMap<>();
                        map.forEach((k, v) -> resolved.put(String.valueOf(k),
                            v instanceof String str ? context.resolveValue(str, new ProgressLoggerEmpty()) : v));
                        field.set(model, resolved);
                    }
                } catch (IllegalAccessException e) {
                    // unreachable with setAccessible(true)
                }
            }
        }
    }

    private void configurePlugins(final ConfigurationModel config,
        final Configuration cfg,
        final boolean ignoreUnrecognizedParameters) {
        final List<String> configuredPluginParameters = ConfigurationExtensionBinder.bind(config,
            cfg.getCurrentEnvironmentName(),
            cfg.getPluginRegister());

        final boolean rootConfigurationsIsEmpty = config.getRootConfigurations().isEmpty();

        final Collection<FlywayException> configurationExceptions = new ArrayList<>();

        try {
            checkUnknownParamsInFlywayNamespace(config.getFlyway(),
                configuredPluginParameters,
                rootConfigurationsIsEmpty,
                "flyway.");
        } catch (final FlywayException e) {
            configurationExceptions.add(e);
        }
        try {
            checkUnknownParamsInFlywayNamespace(config.getEnvironments()
                    .getOrDefault(cfg.getCurrentEnvironmentName(), new EnvironmentModel())
                    .getFlyway(),
                configuredPluginParameters,
                rootConfigurationsIsEmpty,
                "environments." + cfg.getCurrentEnvironmentName() + ".flyway.");
        } catch (final FlywayException e) {
            configurationExceptions.add(e);
        }

        if (!configurationExceptions.isEmpty() && !ignoreUnrecognizedParameters) {
            combineConfigurationExceptions(configurationExceptions);
        }
    }

    private static void makeRelativePathsBasedOnWorkingDirectory(final String workingDirectory,
        final ConfigurationModel config) {
        if (workingDirectory != null) {
            makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getLocations());
            makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getCallbackLocations());
            makeRelativeLocationsInEnvironmentsBasedOnWorkingDirectory(workingDirectory, config.getEnvironments());
            makeRelativeJarDirsBasedOnWorkingDirectory(workingDirectory, config.getFlyway().getJarDirs());
            makeRelativeJarDirsInEnvironmentsBasedOnWorkingDirectory(workingDirectory, config.getEnvironments());
        }
    }

    private static void setDefaultSqlLocation(final String installDirectory, final ClassicConfiguration cfg) {
        final File sqlFolder = new File(installDirectory, DEFAULT_CLI_SQL_LOCATION);
        final Location[] defaultLocations = new Locations(ConfigurationModel.defaults()
            .getFlyway()
            .getLocations()
            .toArray(String[]::new)).getLocations().toArray(Location[]::new);
        if (ConfigUtils.shouldUseDefaultCliSqlLocation(sqlFolder,
            !Arrays.equals(cfg.getLocations(), defaultLocations))) {
            cfg.setLocations(Location.fromPath("filesystem:", sqlFolder.getAbsolutePath()));
        }
    }

    private static void loadJarDirsAndAddToClasspath(final String workingDirectory, final ClassicConfiguration cfg) {
        final List<String> jarDirs = new ArrayList<>();

        final File jarDir = new File(workingDirectory, DEFAULT_CLI_JARS_LOCATION);
        ConfigUtils.warnIfUsingDeprecatedMigrationsFolder(jarDir, ".jar");
        if (jarDir.exists()) {
            jarDirs.add(jarDir.getAbsolutePath());
        }

        jarDirs.addAll(cfg.getJarDirs());

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final List<File> jarFiles = new ArrayList<>();
        jarFiles.addAll(CommandLineConfigurationUtils.getJdbcDriverJarFiles());
        jarFiles.addAll(CommandLineConfigurationUtils.getJavaMigrationJarFiles(jarDirs.toArray(new String[0])));

        if (!jarFiles.isEmpty()) {
            classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
        }

        cfg.setClassLoader(classLoader);
    }

    private void checkUnknownParamsInFlywayNamespace(final FlywayEnvironmentModel flyway,
        final Collection<String> configuredPluginParameters,
        final boolean rootConfigurationsIsEmpty,
        final String prefix) {
        final Map<String, Object> pluginConfigurations = flyway.getPluginConfigurations();

        final Map<String, List<String>> pluginParametersWhichShouldHaveBeenConfigured = getPluginParametersWhichShouldHaveBeenConfigured(
            pluginConfigurations);

        final Map<String, List<String>> missingParams = getUnrecognisedParameters(
            pluginParametersWhichShouldHaveBeenConfigured,
            configuredPluginParameters);

        if (!missingParams.isEmpty()) {
            generateMissingParametersException(flyway, missingParams, rootConfigurationsIsEmpty, prefix);
        }
    }

    private static Map<String, List<String>> getUnrecognisedParameters(final Map<String, List<String>> pluginParametersWhichShouldHaveBeenConfigured,
        final Collection<String> configuredPluginParameters) {
        final Map<String, List<String>> missingParams = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : pluginParametersWhichShouldHaveBeenConfigured.entrySet()) {
            final List<String> missing = entry.getValue()
                .stream()
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

                pluginParametersWhichShouldHaveBeenConfigured.put(configuration.getKey(),
                    temp.keySet().stream().map(Object::toString).toList());
            } else {
                if (!pluginParametersWhichShouldHaveBeenConfigured.containsKey(FLYWAY_NAMESPACE)) {
                    pluginParametersWhichShouldHaveBeenConfigured.put(FLYWAY_NAMESPACE, new ArrayList<>());
                }
                pluginParametersWhichShouldHaveBeenConfigured.get(FLYWAY_NAMESPACE).add(configuration.getKey());
            }
        }
        return pluginParametersWhichShouldHaveBeenConfigured;
    }

    private static void combineConfigurationExceptions(final Collection<? extends FlywayException> configurationExceptions) {
        final StringBuilder exceptionMessage = new StringBuilder("Failed to configure parameters:").append(System.lineSeparator());
        configurationExceptions.forEach(e -> exceptionMessage.append(e.getMessage()).append(System.lineSeparator()));

        final boolean allRecoverable = configurationExceptions.stream()
            .allMatch(ex -> ex.getErrorCode() == CoreErrorCode.CONFIGURATION_RECOVERABLE);

        final FlywayException flywayException = allRecoverable
            ? new ObsoleteConfigurationParametersException(exceptionMessage.toString(),
            mergeObsoleteParameters(configurationExceptions))
            : new FlywayException(exceptionMessage.toString(), CoreErrorCode.CONFIGURATION);
        configurationExceptions.forEach(flywayException::addSuppressed);
        throw flywayException;
    }

    private static List<ObsoleteParameter> mergeObsoleteParameters(final Collection<? extends FlywayException> configurationExceptions) {
        return configurationExceptions.stream()
            .filter(ObsoleteConfigurationParametersException.class::isInstance)
            .map(ObsoleteConfigurationParametersException.class::cast)
            .flatMap(ex -> ex.getObsoleteParameters().stream())
            .collect(Collectors.toList());
    }

    private static void generateMissingParametersException(final FlywayEnvironmentModel model,
        final Map<String, ? extends List<String>> missingParams,
        final boolean rootConfigurationsIsEmpty,
        final String prefix) {

        if (isOSS() && !rootConfigurationsIsEmpty) {
            return;
        }

        final StringBuilder exceptionMessage = new StringBuilder();
        CoreErrorCode errorCode = CoreErrorCode.CONFIGURATION_RECOVERABLE;
        final List<ObsoleteParameter> obsoleteParameters = new ArrayList<>();
        for (final Map.Entry<String, ? extends List<String>> entry : missingParams.entrySet()) {
            final String namespace = entry.getKey();
            final List<String> unknownParams = entry.getValue();
            for (final String param : unknownParams) {
                final UnknownParameterModel unknownParameterModel = resolveUnknownParameter(model,
                    namespace,
                    param,
                    prefix);
                if (unknownParameterModel.kind() == Kind.UNKNOWN) {
                    errorCode = CoreErrorCode.CONFIGURATION;
                } else {
                    obsoleteParameters.add(convertObsoleteParameter(unknownParameterModel));
                }
                exceptionMessage.append(unknownParameterModel).append("\n");
            }
        }

        exceptionMessage.deleteCharAt(exceptionMessage.length() - 1);
        if (errorCode == CoreErrorCode.CONFIGURATION_RECOVERABLE) {
            throw new ObsoleteConfigurationParametersException(exceptionMessage.toString(), obsoleteParameters);
        }
        throw new FlywayException(exceptionMessage.toString(), errorCode);
    }

    private static ObsoleteParameter convertObsoleteParameter(final UnknownParameterModel unknownParameterModel) {
        return switch (unknownParameterModel.kind()) {
            case REPLACED -> new ObsoleteParameter(unknownParameterModel.rawKey(),
                ObsoleteParameter.Kind.REPLACED,
                unknownParameterModel.replacement(),
                unknownParameterModel.reason());
            case REMOVED -> new ObsoleteParameter(unknownParameterModel.rawKey(),
                ObsoleteParameter.Kind.REMOVED,
                null,
                unknownParameterModel.reason());
            case UNKNOWN ->
                throw new IllegalStateException("UNKNOWN parameters should not be converted to ObsoleteParameter");
        };
    }
}
