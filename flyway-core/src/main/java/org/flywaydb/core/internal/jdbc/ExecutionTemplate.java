package org.flywaydb.core.internal.jdbc;

import java.util.concurrent.Callable;

/**
 * Spring-like template for executing operations in the context of a database connection.
 */
public interface ExecutionTemplate {

    /**
     * Executes this callback within the context of the connection
     *
     * @param callback The callback to execute.
     * @return The result of the callback.
     */
    <T> T execute(Callable<T> callback);
}