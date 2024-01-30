package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

@SuppressWarnings("unused")
public interface EnvironmentProvisioner extends Plugin {
    String getName();

    default Class<?> getConfigClass() {
        return null;
    }

    default void setConfiguration(final ConfigurationExtension config) { }

    default void preProvision(final PropertyResolverContext context, final ProgressLogger progress) { }

    default void preReprovision(final PropertyResolverContext context, final ProgressLogger progress) { }

    default void postProvision(final PropertyResolverContext context, final ResolvedEnvironment resolvedEnvironment, final ProgressLogger progress) { }

    default void postReprovision(final PropertyResolverContext context, final ResolvedEnvironment resolvedEnvironment, final ProgressLogger progress) { }
}