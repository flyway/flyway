package org.flywaydb.core.api.errorhandler;

/**
 * An error that occurred while executing a statement.
 */
public interface Error {
    /**
     * @return The error code.
     */
    int getCode();

    /**
     * @return The error state.
     */
    String getState();

    /**
     * @return The error message.
     */
    String getMessage();
}
