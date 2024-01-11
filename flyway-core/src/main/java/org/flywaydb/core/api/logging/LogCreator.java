package org.flywaydb.core.api.logging;

/**
 * Factory for implementation-specific loggers.
 */
public interface LogCreator {
    /**
     * Creates an implementation-specific logger for this class.
     *
     * @param clazz The class to create the logger for.
     * @return The logger.
     */
    Log createLogger(Class<?> clazz);
}