/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.Connection;

/**
 * Convenience base no-op implementation of FlywayCallback. Extend this class if you want to implement just a few
 * callback methods without having to provide no-op methods yourself.
 * <p>
 * <p>This implementation also provides direct access to the {@link FlywayConfiguration} as field.</p>
 *
 * @deprecated Implement {@link Callback} instead. Will be removed in Flyway 6.0.
 */
@Deprecated
public abstract class BaseFlywayCallback implements FlywayCallback, ConfigurationAware {
    private static final Log LOG = LogFactory.getLog(BaseFlywayCallback.class);

    @SuppressWarnings("WeakerAccess")
    protected FlywayConfiguration flywayConfiguration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
        this.flywayConfiguration = flywayConfiguration;
    }

    @Override
    public void beforeClean(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterClean(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeMigrate(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterMigrate(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo info) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeUndo(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeEachUndo(Connection connection, MigrationInfo info) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterEachUndo(Connection connection, MigrationInfo info) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterUndo(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeValidate(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterValidate(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeBaseline(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterBaseline(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeRepair(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterRepair(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void beforeInfo(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }

    @Override
    public void afterInfo(Connection connection) {
        LOG.warn("BaseFlywayCallback has been deprecated and will be removed in Flyway 6.0. Implement org.flywaydb.core.api.callback.Callback instead.");
    }
}