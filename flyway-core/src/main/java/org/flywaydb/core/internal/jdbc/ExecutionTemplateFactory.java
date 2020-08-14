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
package org.flywaydb.core.internal.jdbc;

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
        return createTransactionalExecutionTemplate(connection, true);
    }

    /**
     * Creates a new execution template for this connection.
     * If possible, will attempt to roll back when an exception is thrown.
     *
     * @param connection The connection for execution.
     * @param database The database
     */
    public static ExecutionTemplate createExecutionTemplate(Connection connection, Database database) {
        if (database.supportsMultiStatementTransactions()) {
            return createTransactionalExecutionTemplate(connection, true);
        }

        return new PlainExecutionTemplate();
    }

    /**
     * Creates a new execution template for this connection, which attempts to get exclusive access to the table
     *
     * @param connection          The connection for execution.
     * @param database The database
     */
    public static ExecutionTemplate createTableExclusiveExecutionTemplate(Connection connection, Table table, Database database) {
        if (database.supportsMultiStatementTransactions()) {
            return new TableLockingExecutionTemplate(table, createTransactionalExecutionTemplate(connection, database.supportsDdlTransactions()));
        }

        return new TableLockingExecutionTemplate(table, new PlainExecutionTemplate());
    }

    /**
     * Creates a new transactional execution template for this connection.
     *
     * @param connection          The connection for execution.
     * @param rollbackOnException Whether to attempt to roll back when an exception is thrown.
     */
    private static ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        DatabaseType databaseType = DatabaseType.fromJdbcConnection(connection);

        if (DatabaseType.COCKROACHDB.equals(databaseType)) {
            return new CockroachRetryingTransactionalExecutionTemplate(connection, rollbackOnException);
        }

        return new TransactionalExecutionTemplate(connection, rollbackOnException);
    }
}