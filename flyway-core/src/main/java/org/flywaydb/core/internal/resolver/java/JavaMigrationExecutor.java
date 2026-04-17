/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.resolver.java;

import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adapter for executing migrations implementing JavaMigration.
 */
@RequiredArgsConstructor
public class JavaMigrationExecutor implements MigrationExecutor {
    /**
     * The JavaMigration to execute.
     */
    private final JavaMigration javaMigration;

    private final StatementInterceptor statementInterceptor;

    @Override
    public List<Results> execute(final Context context) throws SQLException {
        if (statementInterceptor != null) {
            statementInterceptor.javaMigration(javaMigration);
        } else {
            DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(context.getConnection(), context.getConfiguration());

            DatabaseExecutionStrategy strategy = databaseType.createExecutionStrategy(context.getConnection());
            strategy.execute(() -> {
                executeOnce(context);
                return true;
            });
        }

        return List.of();
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

    @Override
    public boolean shouldExecute() {
        return true;
    }
}
