/**
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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.spring.ApplicationContextAwareSpringMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;

import java.sql.Connection;

/**
 * Adapter for executing migrations implementing ApplicationContextAwareSpringMigration.
 */
public class ApplicationContextAwareSpringJdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The ApplicationContextAwareSpringMigration to execute.
     */
    private final ApplicationContextAwareSpringMigration springJdbcMigration;

    /**
     * Creates a new ApplicationContextAwareSpringMigration.
     *
     * @param springJdbcMigration The Application Context Aware Spring Jdbc Migration to execute.
     */
    public ApplicationContextAwareSpringJdbcMigrationExecutor(ApplicationContextAwareSpringMigration springJdbcMigration) {
        this.springJdbcMigration = springJdbcMigration;
    }

    @Override
    public void execute(Connection connection) {
        try {
            springJdbcMigration.migrate();
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}
