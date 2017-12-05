/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.api;

/**
 * Exception thrown when Flyway encounters a problem.
 */
public class FlywayException extends RuntimeException {
    /**
     * Creates a new FlywayException with this message and this cause.
     *
     * @param message The exception message.
     * @param cause   The exception cause.
     */
    public FlywayException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new FlywayException with this cause. For use in subclasses that override getMessage().
     *
     * @param cause   The exception cause.
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
