/**
 * Copyright 2010-2016 Boxfuse GmbH
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
