/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.api.configuration;

/**
 * Marks a class as configuration aware (callbacks, resolvers and migrations). Configuration aware classes
 * get the flyway master configuration injected upon creation. The implementer is responsible for correctly storing
 * the provided {@link FlywayConfiguration} (usually in a field).
 */
public interface ConfigurationAware {
    /**
     * Sets the current configuration. This method should not be called directly, it is called by Flyway itself.
     *
     * @param flywayConfiguration The current Flyway configuration.
     */
    void setFlywayConfiguration(FlywayConfiguration flywayConfiguration);
}
