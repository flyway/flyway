/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.resolver.java;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBRetryingStrategy;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adapter for executing migrations implementing JavaMigration.
 */
public class JavaMigrationExecutor implements MigrationExecutor {
    /**
     * The JavaMigration to execute.
     */
    private final JavaMigration javaMigration;

    /**
     * Creates a new JavaMigrationExecutor.
     *
     * @param javaMigration The JavaMigration to execute.
     */
    JavaMigrationExecutor(JavaMigration javaMigration) {
        this.javaMigration = javaMigration;
    }

    @Override
    public void execute(final Context context) throws SQLException {
        DatabaseExecutionStrategy strategy = DatabaseFactory.createExecutionStrategy(context.getConnection());
        strategy.execute(new SqlCallable<Boolean>() {
                @Override
                public Boolean call() throws SQLException {
                    executeOnce(context);
                    return true;
                }
            });
    }

    private void executeOnce(final Context context) throws SQLException {
        try {
            javaMigration.migrate(new org.flywaydb.core.api.migration.Context() {
                @Override
                public Configuration getConfiguration() {
                    return context.getConfiguration();
                }

                @Override
                public Connection getConnection() {
                    return context.getConnection();
                }
            });
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean canExecuteInTransaction() {
        return javaMigration.canExecuteInTransaction();
    }
}