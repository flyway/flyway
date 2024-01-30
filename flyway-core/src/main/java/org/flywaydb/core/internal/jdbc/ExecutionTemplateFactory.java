package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;

import java.sql.Connection;

public class ExecutionTemplateFactory {
    /**
     * Creates a new execution template for this connection.
     * If possible, will attempt to roll back when an exception is thrown.
     *
     * @param connection The connection for execution.
     */
    public static ExecutionTemplate createExecutionTemplate(Connection connection) {
        return createTransactionalExecutionTemplate(connection, true, DatabaseTypeRegister.getDatabaseTypeForConnection(connection));
    }

    /**
     * Creates a new execution template for this connection.
     * If possible, will attempt to roll back when an exception is thrown.
     *
     * @param connection The connection for execution.
     * @param database The database
     */
    public static ExecutionTemplate createExecutionTemplate(Connection connection, Database database) {
        if (database.supportsMultiStatementTransactions() && database.getConfiguration().isExecuteInTransaction()) {
            return createTransactionalExecutionTemplate(connection, true, database.getDatabaseType());
        }

        return new PlainExecutionTemplate();
    }

    /**
     * Creates a new execution template for this connection, which attempts to get exclusive access to the table
     *
     * @param connection The connection for execution.
     * @param database The database
     */
    public static ExecutionTemplate createTableExclusiveExecutionTemplate(Connection connection, Table table, Database database) {
        if (database.supportsMultiStatementTransactions()) {
            return new TableLockingExecutionTemplate(table, createTransactionalExecutionTemplate(connection, database.supportsDdlTransactions(), database.getDatabaseType()));
        }

        return new TableLockingExecutionTemplate(table, new PlainExecutionTemplate());
    }

    /**
     * Creates a new transactional execution template for this connection.
     *
     * @param connection The connection for execution.
     * @param rollbackOnException Whether to attempt to roll back when an exception is thrown.
     */
    private static ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException, DatabaseType databaseType) {
        return databaseType.createTransactionalExecutionTemplate(connection, rollbackOnException);
    }
}