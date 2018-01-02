/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.api.migration.jdbc;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

/**
 * Convenience implementation if {@link JdbcMigration}. {@link ConfigurationAware#setFlywayConfiguration(FlywayConfiguration)}
 * is implemented by storing the configuration in a field. It is encouraged to subclass this class instead of implementing
 * JdbcMigration directly, to guard against possible API additions in future major releases of Flyway.
 */
public abstract class BaseJdbcMigration implements JdbcMigration, ConfigurationAware {

    protected FlywayConfiguration flywayConfiguration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }
}
