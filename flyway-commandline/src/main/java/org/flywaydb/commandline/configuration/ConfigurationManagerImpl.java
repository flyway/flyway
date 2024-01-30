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

import lombok.CustomLog;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class ConfigurationManagerImpl implements ConfigurationManager {

    public Configuration getConfiguration(CommandLineArguments commandLineArguments) {
        ConfigurationManager configurationManager;
        if(useModernConfig(commandLineArguments)) {
            configurationManager = new ModernConfigurationManager();
        } else {
            configurationManager = new LegacyConfigurationManager();
        }
        return configurationManager.getConfiguration(commandLineArguments);
    }

    boolean useModernConfig(final CommandLineArguments commandLineArguments) {

        List<String> configFiles = commandLineArguments.getConfigFiles();

        final List<File> configFilesExist = configFiles.stream()
            .map(File::new)
            .filter(File::exists)
            .toList();

        if (configFilesExist.size() != configFiles.size()) {
            LOG.warn("One or more specified configuration files could not be found: "
                + configFiles.stream()
                .map(File::new)
                .filter(x -> !configFilesExist.contains(x))
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(", ")) + System.lineSeparator());
        }

        final List<File> tomlConfigFiles = configFiles.stream()
            .filter(s -> s.endsWith(".toml"))
            .map(File::new)
            .filter(File::exists)
            .toList();

        final List<File> legacyConfigFiles = configFilesExist.stream()
            .filter(x -> !tomlConfigFiles.contains(x))
            .toList();

        Boolean result = useModernConfigBasedOnFileLists(tomlConfigFiles, legacyConfigFiles, true, null);

        if (result != null) {
            return result;
        }

        List<File> tomlFiles = commandLineArguments.getConfigFilePathsFromEnv(true);
        List<File> legacyFiles = commandLineArguments.getConfigFilePathsFromEnv(false);

        result = useModernConfigBasedOnFileLists(tomlFiles, legacyFiles, true, null);

        if (result != null) {
            return result;
        }

        String workingDirectory = commandLineArguments.getWorkingDirectoryOrNull();

        tomlFiles = ConfigUtils.getDefaultTomlConfigFileLocations(new File(ClassUtils.getInstallDir(Main.class)), workingDirectory).stream()
            .filter(File::exists)
            .toList();
        legacyFiles = ConfigUtils.getDefaultLegacyConfigurationFiles(new File(ClassUtils.getInstallDir(Main.class)), workingDirectory).stream()
            .filter(File::exists)
            .toList();

        return useModernConfigBasedOnFileLists(tomlFiles, legacyFiles, false, true);
    }

    private Boolean useModernConfigBasedOnFileLists(List<File> tomlConfigFiles, List<File> legacyConfigFiles, boolean throwsExceptionIfCoexistent, Boolean defaultResult) {

        if (!tomlConfigFiles.isEmpty()) {
            if (!legacyConfigFiles.isEmpty() && throwsExceptionIfCoexistent) {
                throw new FlywayException(
                    "Using both TOML configuration and CONF configuration is not supported. Please remove the CONF configuration files.\n"
                        +
                        "TOML files: " + tomlConfigFiles.stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.joining(", ")) + System.lineSeparator() +
                        "CONF files: " + legacyConfigFiles.stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.joining(", ")) + System.lineSeparator());
            }

            return true;
        } else {
            if (!legacyConfigFiles.isEmpty()) {
                return false;
            }
        }

        return defaultResult;
    }
}