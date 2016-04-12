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
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;

import java.sql.Connection;

/**
 * Adapter for executing migrations implementing JdbcMigration.
 */
public class JdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The JdbcMigration to execute.
     */
    private final JdbcMigration jdbcMigration;

    /**
     * Creates a new JdbcMigrationExecutor.
     *
     * @param jdbcMigration The JdbcMigration to execute.
     */
    public JdbcMigrationExecutor(JdbcMigration jdbcMigration) {
        this.jdbcMigration = jdbcMigration;
    }

    @Override
    public void execute(Connection connection) {
        try {
            jdbcMigration.migrate(connection);
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}
