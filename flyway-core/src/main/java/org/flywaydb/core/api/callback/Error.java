package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.configuration.Configuration;

/**
 * An error that occurred while executing a statement.
 * <p><i>Flyway Teams only</i></p>
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

    /**
     * Checks whether this error has already been handled.
     *
     * @return {@code true} {@code true} if this error has already be handled or {@code false} if it should flow
     * via the default error handler.
     */
    boolean isHandled();

    /**
     * Sets whether this error has already been handled.
     *
     * @param handled {@code true} if this error has already be handled or {@code false} if it should flow via the
     * default error handler.
     */
    void setHandled(boolean handled, Configuration config);
}