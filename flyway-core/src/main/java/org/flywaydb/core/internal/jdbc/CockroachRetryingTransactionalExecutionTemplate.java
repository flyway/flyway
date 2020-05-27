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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Spring-like template for executing transactions. Cockroach always operates with transaction isolation
 * level SERIALIZABLE and needs a retrying pattern.
 */
public class CockroachRetryingTransactionalExecutionTemplate extends TransactionalExecutionTemplate {
    private static final Log LOG = LogFactory.getLog(CockroachRetryingTransactionalExecutionTemplate.class);

    private static final String DEADLOCK_OR_TIMEOUT_ERROR_CODE = "40001";
    private static final int MAX_RETRIES = 50;

    /**
     * Creates a new transaction template for this connection.
     *
     * @param connection          The connection for the transaction.
     * @param rollbackOnException Whether to roll back the transaction when an exception is thrown.
     */
    CockroachRetryingTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        super(connection, rollbackOnException);
    }

    /**
     * Executes this callback within a transaction
     *
     * @param transactionCallback The callback to execute.
     * @return The result of the transaction code.
     */
    @Override
    public <T> T execute(Callable<T> transactionCallback) {
        // Similar in approach to the CockroachDBRetryingStrategy pattern
        int retryCount = 0;
        while (true) {
            try {
                return transactionCallback.call();
            } catch (SQLException e) {
                if (!DEADLOCK_OR_TIMEOUT_ERROR_CODE.equals(e.getSQLState()) || retryCount >= MAX_RETRIES) {
                    LOG.info("error: " + e);
                    throw new FlywayException(e);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new FlywayException(e);
            }
            retryCount++;
        }
    }
}