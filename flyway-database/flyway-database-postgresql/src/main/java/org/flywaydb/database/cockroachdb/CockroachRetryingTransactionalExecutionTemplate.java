package org.flywaydb.database.cockroachdb;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.TransactionalExecutionTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Spring-like template for executing transactions. Cockroach always operates with transaction isolation
 * level SERIALIZABLE and needs a retrying pattern.
 */
@CustomLog
public class CockroachRetryingTransactionalExecutionTemplate extends TransactionalExecutionTemplate {
    private static final String DEADLOCK_OR_TIMEOUT_ERROR_CODE = "40001";
    private static final int MAX_RETRIES = 50;

    /**
     * Creates a new transaction template for this connection.
     *
     * @param connection The connection for the transaction.
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