package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.FlywayException;

public class EnvironmentVariableResolver implements PropertyResolver {
    @Override
    public String getName() {
        return "env";
    }

    @Override
    public String resolve(String key, PropertyResolverContext context, ProgressLogger progress) {
        String result = System.getenv(key);
        if (result == null) {
            throw new FlywayException("Unable to resolve environment variable: '" + key + "'");
        }
        return result;
    }

    @Override
    public Class getConfigClass() {
        return null;
    }
}