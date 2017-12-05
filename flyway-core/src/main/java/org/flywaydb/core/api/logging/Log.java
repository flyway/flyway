/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.api.logging;

/**
 * A logger.
 */
public interface Log {
    /**
     * Logs a debug message.
     *
     * @param message The message to log.
     */
    void debug(String message);

    /**
     * Logs an info message.
     *
     * @param message The message to log.
     */
    void info(String message);

    /**
     * Logs a warning message.
     *
     * @param message The message to log.
     */
    void warn(String message);

    /**
     * Logs an error message.
     *
     * @param message The message to log.
     */
    void error(String message);

    /**
     * Logs an error message and the exception that caused it.
     *
     * @param message The message to log.
     * @param e The exception that caused the error.
     */
    void error(String message, Exception e);
}
