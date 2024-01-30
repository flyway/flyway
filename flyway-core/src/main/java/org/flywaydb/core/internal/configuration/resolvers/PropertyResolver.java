package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.extensibility.Plugin;

public interface PropertyResolver extends Plugin {
    String getName();
    String resolve(String key, PropertyResolverContext context, ProgressLogger progress);
    Class getConfigClass();
}