/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.api.logging;

import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.logging.android.AndroidLogCreator;
import org.flywaydb.core.internal.util.logging.apachecommons.ApacheCommonsLogCreator;
import org.flywaydb.core.internal.util.logging.javautil.JavaUtilLogCreator;
import org.flywaydb.core.internal.util.logging.slf4j.Slf4jLogCreator;

/**
 * Factory for loggers.
 */
public class LogFactory {
    /**
     * Factory for implementation-specific loggers.
     */
    private static LogCreator logCreator;

    /**
     * The factory for implementation-specific loggers to be used as a fallback when no other suitable loggers were found.
     */
    private static LogCreator fallbackLogCreator;

    /**
     * Prevent instantiation.
     */
    private LogFactory() {
        // Do nothing
    }

    /**
     * @param logCreator The factory for implementation-specific loggers.
     */
    public static void setLogCreator(LogCreator logCreator) {
        LogFactory.logCreator = logCreator;
    }

    /**
     * @param fallbackLogCreator The factory for implementation-specific loggers to be used as a fallback when no other
     *                           suitable loggers were found.
     */
    public static void setFallbackLogCreator(LogCreator fallbackLogCreator) {
        LogFactory.fallbackLogCreator = fallbackLogCreator;
    }

    /**
     * Retrieves the matching logger for this class.
     *
     * @param clazz The class to get the logger for.
     * @return The logger.
     */
    public static Log getLog(Class<?> clazz) {
        if (logCreator == null) {
            FeatureDetector featureDetector = new FeatureDetector(Thread.currentThread().getContextClassLoader());
            if (featureDetector.isAndroidAvailable()) {
                logCreator = new AndroidLogCreator();
            } else if (featureDetector.isSlf4jAvailable()) {
                logCreator = new Slf4jLogCreator();
            } else if (featureDetector.isApacheCommonsLoggingAvailable()) {
                logCreator = new ApacheCommonsLogCreator();
            } else if (fallbackLogCreator == null) {
                logCreator = new JavaUtilLogCreator();
            } else {
                logCreator = fallbackLogCreator;
            }
        }

        return logCreator.createLogger(clazz);
    }
}
