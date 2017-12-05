/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.resolver.jdbc.dummy;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Test migration. Doubles as test for {@link ConfigurationAware} migration.
 */
public class V2__InterfaceBasedMigration implements JdbcMigration, ConfigurationAware {

    private FlywayConfiguration flywayConfiguration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }

    public void migrate(Connection connection) throws Exception {
        if (flywayConfiguration == null) {
            throw new FlywayException("Flyway configuration has not been set on migration");
        }
        // Do nothing else
    }

    public boolean isFlywayConfigurationSet() {
        return flywayConfiguration != null;
    }
}
