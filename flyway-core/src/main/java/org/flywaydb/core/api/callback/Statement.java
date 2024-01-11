package org.flywaydb.core.api.callback;

import java.util.List;

/**
 * The statement relevant to an event.
 * <p><i>Flyway Teams only</i></p>
 */
public interface Statement {
    /**
     * @return The SQL statement.
     */
    String getSql();

    /**
     * @return The warnings that were raised during the execution of the statement.
     * {@code null} if the statement hasn't been executed yet.
     */
    List<Warning> getWarnings();

    /**
     * @return The errors that were thrown during the execution of the statement.
     * {@code null} if the statement hasn't been executed yet.
     */
    List<Error> getErrors();
}