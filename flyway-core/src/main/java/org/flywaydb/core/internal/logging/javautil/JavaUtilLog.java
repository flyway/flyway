package org.flywaydb.core.internal.logging.javautil;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class JavaUtilLog implements Log {

    private final Logger logger;

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
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

    public void notice(String message) {}

    /**
     * Log the message at the specified level with the specified exception if any.
     */
    private void log(Level level, String message, Exception e) {
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