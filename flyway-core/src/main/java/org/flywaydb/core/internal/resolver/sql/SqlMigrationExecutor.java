/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.internal.resolver.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;

import java.sql.SQLException;

/**
 * Database migration based on a sql file.
 */
@RequiredArgsConstructor
public class SqlMigrationExecutor implements MigrationExecutor {
    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;

    /**
     * The SQL script that will be executed.
     */
    private final SqlScript sqlScript;

    /**
     * Whether this is part of an undo migration or a regular one.
     */
    private final boolean undo;

    /**
     * Whether to batch SQL statements.
     */
    private final boolean batch;

    @Override
    public List<Results> execute(final Context context) throws SQLException {
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(context.getConnection(), context.getConfiguration());

        DatabaseExecutionStrategy strategy = databaseType.createExecutionStrategy(context.getConnection());
        return strategy.execute(() -> {
            return executeOnce(context);
        });
    }

    private List<Results> executeOnce(Context context) {

        boolean outputQueryResults = context.getConfiguration().isOutputQueryResults();

        var executorFactory = sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection(), undo, batch, outputQueryResults);
        return executorFactory.execute(sqlScript, context.getConfiguration());
    }

    @Override
    public boolean canExecuteInTransaction() {
        return sqlScript.executeInTransaction();
    }

    @Override
    public boolean shouldExecute() {
        return sqlScript.shouldExecute();
    }

    @Override
    public String shouldExecuteExpression() {
        return sqlScript.shouldExecuteExpression();
    }
}
