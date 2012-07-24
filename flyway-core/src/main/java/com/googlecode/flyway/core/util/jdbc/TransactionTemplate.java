/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util.jdbc;

import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Spring-like template for executing transactions.
 */
public class TransactionTemplate {
    private static final Log LOG = LogFactory.getLog(TransactionTemplate.class);

    /**
     * The connection for the transaction.
     */
    private final Connection connection;

    /**
     * Creates a new transaction template for this connection.
     *
     * @param connection The connection for the transaction.
     */
    public TransactionTemplate(Connection connection) {
        this.connection = connection;
    }

    /**
     * Executes this callback within a transaction.
     *
     * @param transactionCallback The callback to execute.
     * @return The result of the transaction code.
     * @throws TransactionException when the transaction execution failed.
     */
    public <T> T execute(TransactionCallback<T> transactionCallback) throws TransactionException {
        try {
            connection.setAutoCommit(false);
            T result = transactionCallback.doInTransaction();
            connection.commit();
            return result;
        } catch (SQLException e) {
            try {
                connection.rollback();
                throw new TransactionException("Transaction failed!", e);
            } catch (SQLException se) {
                LOG.error("Unable to rollback transaction", e);
                throw new TransactionException("Error while executing transaction. Roll back failed!", e);
            }
        } catch (RuntimeException e) {
            try {
                connection.rollback();
                throw e;
            } catch (SQLException se) {
                LOG.error("Unable to rollback transaction", e);
                throw new TransactionException("Error while executing transaction. Roll back failed!", e);
            }
        }
    }
}
