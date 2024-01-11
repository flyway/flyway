package org.flywaydb.commandline.configuration;

import lombok.CustomLog;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.flywaydb.commandline.configuration.CommandLineConfigurationUtils.getTomlConfigFilePaths;

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

    private boolean useModernConfig(final CommandLineArguments commandLineArguments) {

        final List<File> tomlConfigFiles = commandLineArguments.getConfigFiles().stream()
            .filter(s -> s.endsWith(".toml"))
            .map(File::new)
            .toList();

        if (!tomlConfigFiles.isEmpty()) {
            final List<File> configFilesExist = commandLineArguments.getConfigFiles().stream()
                .map(File::new)
                .filter(File::exists)
                .toList();
            if (configFilesExist.size() != tomlConfigFiles.size()) {
                throw new FlywayException(
                    "Using both TOML configuration and CONF configuration is not supported. Please remove the CONF configuration files.\n"
                        +
                        "TOML files: " + tomlConfigFiles.stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.joining(", ")) + "\n" +
                        "CONF files: " + configFilesExist.stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.joining(", ")) + "\n");
            }
        }

        if (tomlConfigFiles.stream().anyMatch(File::exists)) {
            return true;
        }

        final List<File> tomlFiles = new ArrayList<>();
        tomlFiles.addAll(ConfigUtils.getDefaultTomlConfigFileLocations(new File(ClassUtils.getInstallDir(Main.class))));
        tomlFiles.addAll(getTomlConfigFilePaths());
        return tomlFiles.stream().anyMatch(File::exists);
    }
}