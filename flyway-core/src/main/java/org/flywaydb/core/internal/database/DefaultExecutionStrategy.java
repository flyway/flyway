package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

/**
 * The default execution strategy for a {@code SQLCallable}, which performs a single execution.
 */
public class DefaultExecutionStrategy implements DatabaseExecutionStrategy {
    public <T> T execute(final SqlCallable<T> callable) throws SQLException {
        return callable.call();
    }
}