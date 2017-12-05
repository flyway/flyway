/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.maven;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.apache.maven.plugin.AbstractMojo;

/**
 * Log Creator for Maven Logging.
 */
public class MavenLogCreator implements LogCreator {
    /**
     * The Maven Mojo to log for.
     */
    private final AbstractMojo mojo;

    /**
     * Creates a new Maven Log Creator for this Mojo.
     *
     * @param mojo The Maven Mojo to log for.
     */
    MavenLogCreator(AbstractMojo mojo) {
        this.mojo = mojo;
    }

    public Log createLogger(Class<?> clazz) {
        return new MavenLog(mojo.getLog());
    }
}
