package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.FlywayException;

public class EnvironmentProvisionerNone implements EnvironmentProvisioner {
    @Override
    public String getName() {
        return "none";
    }

    @Override
    public void preReprovision(PropertyResolverContext context, ProgressLogger progress) {
        throw new FlywayException("Reprovisioning is not supported for environment " + context.getEnvironmentName());
    }
}