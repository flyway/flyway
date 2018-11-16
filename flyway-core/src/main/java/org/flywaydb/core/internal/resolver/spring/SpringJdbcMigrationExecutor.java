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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.SQLException;

/**
 * Adapter for executing migrations implementing SpringJdbcMigration.
 */
public class SpringJdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The SpringJdbcMigration to execute.
     */
    private final SpringJdbcMigration springJdbcMigration;

    /**
     * Creates a new SpringJdbcMigrationExecutor.
     *
     * @param springJdbcMigration The Spring Jdbc Migration to execute.
     */
    SpringJdbcMigrationExecutor(SpringJdbcMigration springJdbcMigration) {
        this.springJdbcMigration = springJdbcMigration;
    }

    @Override
    public void execute(Context context) throws SQLException {
        try {
            springJdbcMigration.migrate(new org.springframework.jdbc.core.JdbcTemplate(
                    new SingleConnectionDataSource(context.getConnection(), true)));
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }
}