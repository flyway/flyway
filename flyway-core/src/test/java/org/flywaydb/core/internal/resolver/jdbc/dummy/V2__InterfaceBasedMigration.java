/*
 * Copyright 2010-2017 Boxfuse GmbH
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
