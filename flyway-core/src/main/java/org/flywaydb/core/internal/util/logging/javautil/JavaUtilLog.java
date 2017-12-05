/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.logging.javautil;

import org.flywaydb.core.api.logging.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Wrapper for a java.util.Logger.
 */
public class JavaUtilLog implements Log {
    /**
     * Java Util Logger.
     */
    private final Logger logger;

    /**
     * Creates a new wrapper around this logger.
     *
     * @param logger The original java.util Logger.
     */
    public JavaUtilLog(Logger logger) {
        this.logger = logger;
    }

    public void debug(String message) {
        log(Level.FINE, message, null);
    }

    public void info(String message) {
        log(Level.INFO, message, null);
    }

    public void warn(String message) {
        log(Level.WARNING, message, null);
    }

    public void error(String message) {
        log(Level.SEVERE, message, null);
    }

    public void error(String message, Exception e) {
        log(Level.SEVERE, message, e);
    }

    /**
     * Log the message at the specified level with the specified exception if any.
     *
     * @param level The level to log at.
     * @param message The message to log.
     * @param e The exception, if any.
     */
    private void log(Level level, String message, Exception e) {
        // millis and thread are filled by the constructor
        LogRecord record = new LogRecord(level, message);
        record.setLoggerName(logger.getName());
        record.setThrown(e);
        record.setSourceClassName(logger.getName());
        record.setSourceMethodName(getMethodName());
        logger.log(record);
    }

    /**
     * Computes the source method name for the log output.
     */
    private String getMethodName() {
        StackTraceElement[] steArray = new Throwable().getStackTrace();

        for (StackTraceElement stackTraceElement : steArray) {
            if (logger.getName().equals(stackTraceElement.getClassName())) {
                return stackTraceElement.getMethodName();
            }
        }

        return null;
    }
}
