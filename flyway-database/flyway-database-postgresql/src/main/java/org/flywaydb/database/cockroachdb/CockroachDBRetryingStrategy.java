package org.flywaydb.database.cockroachdb;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

/**
 * CockroachDB recommend the use of retries should we see a SQL error code 40001, which represents a lock wait timeout.
 * This class implements an appropriate retry pattern.
 */
@CustomLog
public class CockroachDBRetryingStrategy implements DatabaseExecutionStrategy {
    private static final String DEADLOCK_OR_TIMEOUT_ERROR_CODE = "40001";
    private static final int MAX_RETRIES = 50;

    public <T> T execute(final SqlCallable<T> callable) throws SQLException {
        int retryCount = 0;
        while (true) {
            try {
                return callable.call();
            } catch (SQLException e) {
                checkRetryOrThrow(e, retryCount);
                retryCount++;
            }
        }
    }

    void checkRetryOrThrow(SQLException e, int retryCount) throws SQLException {
        if (DEADLOCK_OR_TIMEOUT_ERROR_CODE.equals(e.getSQLState()) && retryCount < MAX_RETRIES) {
            LOG.info("Retrying because of deadlock or timeout: " + e.getMessage());
        }
        // Exception is non-retryable
        throw e;
    }
}