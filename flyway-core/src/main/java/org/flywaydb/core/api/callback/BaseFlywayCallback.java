/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

import java.sql.Connection;

/**
 * Convenience base no-op implementation of FlywayCallback. Extend this class if you want to implement just a few
 * callback methods without having to provide no-op methods yourself.
 *
 * <p>This implementation also provides direct access to the {@link FlywayConfiguration} as field.</p>
 */
public abstract class BaseFlywayCallback implements FlywayCallback, ConfigurationAware {
    @SuppressWarnings("WeakerAccess")
    protected FlywayConfiguration flywayConfiguration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }

    @Override
    public void beforeClean(Connection connection) {
    }

    @Override
    public void afterClean(Connection connection) {
    }

    @Override
    public void beforeMigrate(Connection connection) {
    }

    @Override
    public void afterMigrate(Connection connection) {
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {
    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo info) {
    }

    @Override
    public void beforeUndo(Connection connection) {
    }

    @Override
    public void beforeEachUndo(Connection connection, MigrationInfo info) {
    }

    @Override
    public void afterEachUndo(Connection connection, MigrationInfo info) {
    }

    @Override
    public void afterUndo(Connection connection) {
    }

    @Override
    public void beforeValidate(Connection connection) {
    }

    @Override
    public void afterValidate(Connection connection) {
    }

    @Override
    public void beforeBaseline(Connection connection) {
    }

    @Override
    public void afterBaseline(Connection connection) {
    }

    @Override
    public void beforeRepair(Connection connection) {
    }

    @Override
    public void afterRepair(Connection connection) {
    }

    @Override
    public void beforeInfo(Connection connection) {
    }

    @Override
    public void afterInfo(Connection connection) {
    }
}
