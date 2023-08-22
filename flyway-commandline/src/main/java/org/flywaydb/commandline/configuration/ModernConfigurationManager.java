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
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.TomlUtils;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        ConfigurationModel commandLineArgumentsModel = TomlUtils.loadConfigurationFromCommandlineArgs(commandLineArguments.getConfiguration());
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


        config.getFlyway().getLocations().add("filesystem:" + new File(workingDirectory, "sql").getAbsolutePath());

        List<String> jarDirs = new ArrayList<>();

        File jarDir = new File(workingDirectory, "jars");
        if (jarDir.exists()) {
            jarDirs.add(jarDir.getAbsolutePath());
        }

        String configuredJarDirs = commandLineArguments.getConfiguration().get(ConfigUtils.JAR_DIRS);
        if (StringUtils.hasText(configuredJarDirs)) {
            jarDirs.addAll(StringUtils.tokenizeToStringCollection(configuredJarDirs.replace(File.pathSeparator, ","), ","));
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<File> jarFiles = new ArrayList<>();
        jarFiles.addAll(CommandLineConfigurationUtils.getJdbcDriverJarFiles());
        jarFiles.addAll(CommandLineConfigurationUtils.getJavaMigrationJarFiles(jarDirs.toArray(new String[0])));

        if (!jarFiles.isEmpty()) {
            classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
        }

        ConfigUtils.dumpConfigurationModel(config);
        ClassicConfiguration cfg = new ClassicConfiguration(config);
        cfg.setClassLoader(classLoader);

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> missingParams = new ArrayList<>();
        for (ConfigurationExtension configurationExtension : cfg.getPluginRegister().getPlugins(ConfigurationExtension.class)) {
            String namespace = configurationExtension.getNamespace();
            if (config.getFlyway().getPluginConfigurations().containsKey(namespace)) {
                List<String> fields = Arrays.stream(configurationExtension.getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
                Map<String, Object> values = (Map<String, Object>) config.getFlyway().getPluginConfigurations().get(namespace);
                values = values.entrySet().stream().filter(p -> fields.contains(p.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                for (String key : values.keySet()) {
                    ((Map<?, ?>) config.getFlyway().getPluginConfigurations().get(namespace)).remove(key);
                }
                try {
                    ConfigurationExtension newConfigurationExtension = objectMapper.convertValue(values, configurationExtension.getClass());
                    MergeUtils.mergeModel(newConfigurationExtension, configurationExtension);
                } catch (IllegalArgumentException e) {
                    Matcher matcher = ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN.matcher(e.getMessage());
                    if (matcher.find()) {
                        missingParams.add(matcher.group(1));
                    }
                }
            }
        }
        Map<String, Object> pluginConfigurations = config.getFlyway().getPluginConfigurations();
        pluginConfigurations.remove("jarDirs");
        for (Map.Entry<String, Object> configuration : pluginConfigurations.entrySet()) {
            if (configuration.getValue() instanceof Map<?, ?>) {
                Map<String, Object> temp = (Map<String, Object>) configuration.getValue();
                missingParams.addAll(temp.keySet());
            } else {
                missingParams.add(configuration.getKey());
            }
        }

        if (!missingParams.isEmpty()) {
            throw new FlywayException("Unknown configuration parameters: " + missingParams.stream().collect(Collectors.joining(", ")));
        }







        return cfg;
    }
}