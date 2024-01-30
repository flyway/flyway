package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

/**
 * Defines a strategy for executing a {@code SqlCallable} against a particular database.
 */
public interface DatabaseExecutionStrategy {

    /**
     * Execute the given callable using the defined strategy.
     *
     * @param callable The SQL callable to execute.
     * @param <T> The return type of the SQL callable.
     * @return The object returned by the SQL callable.
     */
    <T> T execute(final SqlCallable<T> callable) throws SQLException;
}