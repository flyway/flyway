package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.errorhandler.Error;

public class ErrorImpl implements Error {
    private final int code;
    private final String state;
    private final String message;

    /**
     * An error that occurred while executing a statement.
     * @param code The error code.
     * @param state The error state.
     * @param message The error message.
     */
    public ErrorImpl(int code, String state, String message) {
        this.code = code;
        this.state = state;
        this.message = message;
    }

    /**
     * @return The error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return The error state.
     */
    public String getState() {
        return state;
    }

    /**
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }
}
