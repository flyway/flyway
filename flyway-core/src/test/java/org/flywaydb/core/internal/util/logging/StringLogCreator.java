/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.logging;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

/**
 * Log creator for capturing the output as a string.
 */
public class StringLogCreator implements LogCreator {
    private final StringBuilder output = new StringBuilder();

    public Log createLogger(Class<?> clazz) {
        return new StringLog(output, false);
    }

    public String getOutput() {
        return output.toString();
    }
}
