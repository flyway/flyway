package org.flywaydb.core.api;

import lombok.Getter;

/**
 * Exception thrown when Flyway encounters a problem.
 */
public class FlywayException extends RuntimeException {

    @Getter
    private ErrorCode errorCode = ErrorCode.ERROR;

    /**
     * Creates a new FlywayException with this message, cause, and error code.
     *
     * @param message The exception message.
     * @param cause The exception cause.
     * @param errorCode The error code.
     */
    public FlywayException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Creates a new FlywayException with this message and error code
     *
     * @param message The exception message.
     * @param errorCode The error code.
     */
    public FlywayException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates a new FlywayException with this message and this cause.
     *
     * @param message The exception message.
     * @param cause The exception cause.
     */
    public FlywayException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new FlywayException with this cause. For use in subclasses that override getMessage().
     *
     * @param cause The exception cause.
     */
    public FlywayException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new FlywayException with this message.
     *
     * @param message The exception message.
     */
    public FlywayException(String message) {
        super(message);
    }

    /**
     * Creates a new FlywayException. For use in subclasses that override getMessage().
     */
    public FlywayException() {
        super();
    }
}