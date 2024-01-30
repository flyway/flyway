package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.configuration.Configuration;

import java.util.List;
import org.flywaydb.core.extensibility.ConfigurationExtension;

public interface PropertyResolverContext {
    Configuration getConfiguration();
    String getWorkingDirectory();
    String getEnvironmentName();
    String resolveValue(String input, ProgressLogger progress);
    String resolveValueOrThrow(String input, ProgressLogger progress, String propertyName);
    List<String> resolveValues(List<String> input, ProgressLogger progress);
    List<String> resolveValuesOrThrow(List<String> input, ProgressLogger progress, String propertyName);
    ConfigurationExtension getResolverConfiguration(String resolverName);

}