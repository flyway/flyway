package org.flywaydb.core.extensibility;

import org.flywaydb.core.api.configuration.ClassicConfiguration;

import java.util.Map;

public interface ConfigurationProvider<T extends ConfigurationExtension> extends Plugin {
    Map<String, String> getConfiguration(T configurationExtension, ClassicConfiguration flywayConfiguration) throws Exception;
    Class<T> getConfigurationExtensionClass();
}