package org.flywaydb.core.internal.resolver.sql;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
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
    public void execute(final Context context) throws SQLException {
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(context.getConnection());

        DatabaseExecutionStrategy strategy = databaseType.createExecutionStrategy(context.getConnection());
        strategy.execute(() -> {
            executeOnce(context);
            return true;
        });
    }

    private void executeOnce(Context context) {
        boolean outputQueryResults = false;




        sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection(), undo, batch, outputQueryResults).execute(sqlScript, context.getConfiguration());
    }

    @Override
    public boolean canExecuteInTransaction() {
        return sqlScript.executeInTransaction();
    }

    @Override
    public boolean shouldExecute() {
        return sqlScript.shouldExecute();
    }
}