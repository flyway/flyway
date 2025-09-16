/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import lombok.CustomLog;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_JARS_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.DEFAULT_CLI_SQL_LOCATION;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeJarDirsBasedOnWorkingDirectory;
import static org.flywaydb.core.internal.configuration.ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory;

@CustomLog
public class LegacyConfigurationManager implements ConfigurationManager {

    public Configuration getConfiguration(CommandLineArguments commandLineArguments) {

        Map<String, String> config = new HashMap<>();
        String installDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : ClassUtils.getInstallDir(Main.class);
        String workingDirectory = commandLineArguments.getWorkingDirectoryOrNull();

        File jarDir = new File(installDirectory, DEFAULT_CLI_JARS_LOCATION);
        ConfigUtils.warnIfUsingDeprecatedMigrationsFolder(jarDir, ".jar");
        if (jarDir.exists()) {
            config.put(ConfigUtils.JAR_DIRS, jarDir.getAbsolutePath());
        }

        Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

        loadConfigurationFromConfigFiles(config, commandLineArguments, envVars);

        config.putAll(envVars);
        config = overrideConfiguration(config, commandLineArguments.getConfiguration(false));

        File sqlFolder = new File(installDirectory, DEFAULT_CLI_SQL_LOCATION);
        if (ConfigUtils.shouldUseDefaultCliSqlLocation(sqlFolder, StringUtils.hasText(config.get(ConfigUtils.LOCATIONS)))) {
            config.put(ConfigUtils.LOCATIONS, "filesystem:" + sqlFolder.getAbsolutePath());
        }

        if (workingDirectory != null) {
            makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config, ConfigUtils.LOCATIONS);
            makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory, config, ConfigUtils.CALLBACK_LOCATIONS);
            makeRelativeJarDirsBasedOnWorkingDirectory(workingDirectory, config);
        }

        ClassLoader classLoader = buildClassLoaderBasedOnJarDirs(Thread.currentThread().getContextClassLoader(), config);

        ConfigUtils.dumpConfigurationMap(config, "Using configuration:");
        filterProperties(config);

        return new FluentConfiguration(classLoader).configuration(config).workingDirectory(workingDirectory);
    }

    protected void loadConfigurationFromConfigFiles(Map<String, String> config, CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(commandLineArguments, envVars);
        File installationDir = new File(ClassUtils.getInstallDir(Main.class));
        String workingDirectory = commandLineArguments.getWorkingDirectoryOrNull();

        config.putAll(ConfigUtils.loadDefaultConfigurationFiles(installationDir, workingDirectory, encoding));

        for (File configFile : determineLegacyConfigFilesFromArgs(commandLineArguments)) {
            config.putAll(ConfigUtils.loadConfigurationFile(configFile, encoding, true));
        }
    }

    /**
     * @return The encoding. (default: UTF-8)
     */
    private static String determineConfigurationFileEncoding(CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        if (envVars.containsKey(ConfigUtils.CONFIG_FILE_ENCODING)) {
            return envVars.get(ConfigUtils.CONFIG_FILE_ENCODING);
        }

        if (commandLineArguments.isConfigFileEncodingSet()) {
            return commandLineArguments.getConfigFileEncoding();
        }

        return "UTF-8";
    }

    private static List<File> determineLegacyConfigFilesFromArgs(CommandLineArguments commandLineArguments) {

        List<File> legacyFiles = commandLineArguments.getConfigFilePathsFromEnv(false);
        legacyFiles.addAll(commandLineArguments.getConfigFiles().stream().filter(s -> !s.endsWith(".toml")).map(File::new).toList());

        return legacyFiles;
    }

    private static Map<String, String> overrideConfiguration(Map<String, String> existingConfiguration, Map<String, String> newConfiguration) {
        Map<String, String> combinedConfiguration = new HashMap<>();

        combinedConfiguration.putAll(existingConfiguration);
        combinedConfiguration.putAll(newConfiguration);

        return combinedConfiguration;
    }

    /**
     * Filters the properties to remove the Flyway Commandline-specific ones.
     */
    private void filterProperties(Map<String, String> config) {
        config.remove(ConfigUtils.CONFIG_FILES);
        config.remove(ConfigUtils.CONFIG_FILE_ENCODING);
    }

    private ClassLoader buildClassLoaderBasedOnJarDirs(ClassLoader parentClassLoader, Map<String, String> config) {
        List<File> jarFiles = new ArrayList<>(CommandLineConfigurationUtils.getJdbcDriverJarFiles());

        String jarDirs = config.get(ConfigUtils.JAR_DIRS);

        if (StringUtils.hasText(jarDirs)) {
            jarFiles.addAll(CommandLineConfigurationUtils.getJavaMigrationJarFiles(StringUtils.tokenizeToStringArray(jarDirs.replace(File.pathSeparator, ","), ",")));
        }

        if (!jarFiles.isEmpty()) {
            return ClassUtils.addJarsOrDirectoriesToClasspath(parentClassLoader, jarFiles);
        }

        return parentClassLoader;
    }
}
