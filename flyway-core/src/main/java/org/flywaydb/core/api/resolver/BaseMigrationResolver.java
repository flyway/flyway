package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

/**
 * Base implementation of {@link MigrationResolver} that handles configuration injections by storing the
 * configuration object in a field.
 */
public abstract class BaseMigrationResolver implements MigrationResolver, ConfigurationAware {

    protected FlywayConfiguration flywayConfiguration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }
}
