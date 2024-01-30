package org.flywaydb.commandline.configuration;

import org.flywaydb.core.api.configuration.Configuration;

public interface ConfigurationManager {

    Configuration getConfiguration(CommandLineArguments commandLineArguments);
}