/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.maven;

import org.flywaydb.core.api.logging.Log;

/**
 * Wrapper around a Maven Logger.
 */
public class MavenLog implements Log {
    /**
     * Maven Logger.
     */
    private final org.apache.maven.plugin.logging.Log logger;

    /**
     * Creates a new wrapper around this logger.
     *
     * @param logger The original Maven Logger.
     */
    MavenLog(org.apache.maven.plugin.logging.Log logger) {
        this.logger = logger;
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Exception e) {
        logger.error(message, e);
    }
}
