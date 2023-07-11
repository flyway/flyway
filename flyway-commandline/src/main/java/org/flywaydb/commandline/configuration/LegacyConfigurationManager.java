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

import org.flywaydb.commandline.Main;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.Console;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LegacyConfigurationManager implements ConfigurationManager {

    public Configuration getConfiguration(CommandLineArguments commandLineArguments) {

        Map<String, String> config = new HashMap<>();
        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : ClassUtils.getInstallDir(Main.class);

        config.put(ConfigUtils.LOCATIONS, "filesystem:" + new File(workingDirectory, "sql").getAbsolutePath());

        File jarDir = new File(workingDirectory, "jars");
        if (jarDir.exists()) {
            config.put(ConfigUtils.JAR_DIRS, jarDir.getAbsolutePath());
        }

        Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

        loadConfigurationFromConfigFiles(config, commandLineArguments, envVars);

        config.putAll(envVars);
        config = overrideConfiguration(config, commandLineArguments.getConfiguration());

        if (commandLineArguments.isWorkingDirectorySet()) {
            makeRelativeLocationsBasedOnWorkingDirectory(commandLineArguments, config);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<File> jarFiles = new ArrayList<>(CommandLineConfigurationUtils.getJdbcDriverJarFiles());

        String jarDirs = config.get(ConfigUtils.JAR_DIRS);
        if (StringUtils.hasText(jarDirs)) {
            jarFiles.addAll(CommandLineConfigurationUtils.getJavaMigrationJarFiles(StringUtils.tokenizeToStringArray(jarDirs.replace(File.pathSeparator, ","), ",")));
        }

        if (!jarFiles.isEmpty()) {
            classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
        }

        if (!commandLineArguments.shouldSuppressPrompt()) {
            promptForCredentialsIfMissing(config);
        }

        ConfigUtils.dumpConfiguration(config);
        filterProperties(config);







        return new FluentConfiguration(classLoader).configuration(config);
    }

    protected void loadConfigurationFromConfigFiles(Map<String, String> config, CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(commandLineArguments, envVars);
        File installationDir = new File(ClassUtils.getInstallDir(Main.class));

        config.putAll(ConfigUtils.loadDefaultConfigurationFiles(installationDir, encoding));

        for (File configFile : determineLegacyConfigFilesFromArgs(commandLineArguments, envVars)) {
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

    private static List<File> determineLegacyConfigFilesFromArgs(CommandLineArguments commandLineArguments, Map<String, String> envVars) {

        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : null;

        Stream<String> configFilePaths = envVars.containsKey(ConfigUtils.CONFIG_FILES) ?
                Arrays.stream(StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIG_FILES), ",")) :
                commandLineArguments.getConfigFiles().stream();

        return configFilePaths.map(path -> Paths.get(path).isAbsolute() ? new File(path) : new File(workingDirectory, path)).collect(Collectors.toList());
    }

    private static Map<String, String> overrideConfiguration(Map<String, String> existingConfiguration, Map<String, String> newConfiguration) {
        Map<String, String> combinedConfiguration = new HashMap<>();

        combinedConfiguration.putAll(existingConfiguration);
        combinedConfiguration.putAll(newConfiguration);

        return combinedConfiguration;
    }

    private static void makeRelativeLocationsBasedOnWorkingDirectory(CommandLineArguments commandLineArguments, Map<String, String> config) {
        String[] locations = config.get(ConfigUtils.LOCATIONS).split(",");
        for (int i = 0; i < locations.length; i++) {
            if (locations[i].startsWith(Location.FILESYSTEM_PREFIX)) {
                String newLocation = locations[i].substring(Location.FILESYSTEM_PREFIX.length());
                File file = new File(newLocation);
                if (!file.isAbsolute()) {
                    file = new File(commandLineArguments.getWorkingDirectory(), newLocation);
                }
                locations[i] = Location.FILESYSTEM_PREFIX + file.getAbsolutePath();
            }
        }

        config.put(ConfigUtils.LOCATIONS, StringUtils.arrayToCommaDelimitedString(locations));
    }

    /**
     * If no user or password has been provided, prompt for it. If you want to avoid the prompt, pass in an empty
     * user or password.
     *
     * @param config The properties object to load to configuration into.
     */
    private void promptForCredentialsIfMissing(Map<String, String> config) {
        Console console = System.console();
        if (console == null) {
            // We are running in an automated build. Prompting is not possible.
            return;
        }

        if (!config.containsKey(ConfigUtils.URL)) {
            // URL is not set. We are doomed for failure anyway.
            return;
        }

        String url = config.get(ConfigUtils.URL);

        boolean hasUser = config.containsKey(ConfigUtils.USER) || config.keySet().stream().anyMatch(p -> p.toLowerCase().endsWith(".user"));

        if (!hasUser



                && needsUser(url, config.getOrDefault(ConfigUtils.PASSWORD, null))) {
            config.put(ConfigUtils.USER, console.readLine("Database user: "));
        }

        boolean hasPassword = config.containsKey(ConfigUtils.PASSWORD) || config.keySet().stream().anyMatch(p -> p.toLowerCase().endsWith(".password"));

        if (!hasPassword



                && needsPassword(url, config.get(ConfigUtils.USER))) {
            char[] password = console.readPassword("Database password: ");
            config.put(ConfigUtils.PASSWORD, password == null ? "" : String.valueOf(password));
        }
    }

    /**
     * Detect whether the JDBC URL specifies a known authentication mechanism that does not need a username.
     */
    boolean needsUser(String url, String password) {
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForUrl(url);
        if (databaseType.detectUserRequiredByUrl(url)) {








             return true;

        }

        return false;
    }

    /**
     * Detect whether the JDBC URL specifies a known authentication mechanism that does not need a password.
     */
    boolean needsPassword(String url, String username) {
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForUrl(url);
        if (databaseType.detectPasswordRequiredByUrl(url)) {








             return true;

        }

        return false;
    }

    /**
     * Filters the properties to remove the Flyway Commandline-specific ones.
     */
    private void filterProperties(Map<String, String> config) {
        config.remove(ConfigUtils.JAR_DIRS);
        config.remove(ConfigUtils.CONFIG_FILES);
        config.remove(ConfigUtils.CONFIG_FILE_ENCODING);
    }
}