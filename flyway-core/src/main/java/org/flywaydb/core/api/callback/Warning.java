package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.configuration.Configuration;

/**
 * A warning that occurred while executing a statement.
 * <p><i>Flyway Teams only</i></p>
 */
public interface Warning {
    /**
     * @return The warning code.
     */
    int getCode();

    /**
     * @return The warning state.
     */
    String getState();

    /**
     * @return The warning message.
     */
    String getMessage();

    /**
     * Checks whether this warning has already been handled.
     *
     * @return {@code true} {@code true} if this warning has already be handled or {@code false} if it should flow
     * via the default warning handler.
     */
    boolean isHandled();

    /**
     * Sets whether this warning has already been handled.
     *
     * @param handled {@code true} if this warning has already be handled or {@code false} if it should flow via the
     * default warning handler.
     */
    void setHandled(boolean handled, Configuration config);
}