package com.googlecode.flyway.core.init;

import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.SchemaVersion;

/**
 * Exception indicating that migration failed.
 */
public class InitException extends FlywayException {
    /**
     * Creates a new InitException with this error message and this cause.
     *
     * @param message The error message.
     * @param cause   The exception that caused this.
     */
    public InitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new InitException with this error message.
     *
     * @param message The error message.
     */
    public InitException(String message) {
        super(message);
    }
}
