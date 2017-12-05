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
package org.flywaydb.core.internal.util.logging;

import org.flywaydb.core.api.logging.Log;

/**
 * Logger that captures output as a string.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class StringLog implements Log {
    private final boolean debugEnabled;

    private final StringBuilder output;

    public StringLog(StringBuilder output, boolean debugEnabled) {
        this.output = output;
        this.debugEnabled = debugEnabled;
    }

    public void debug(String message) {
        if (debugEnabled) {
            output.append("DEBUG: " + message + "\n");
        }
    }

    public void info(String message) {
        output.append("INFO: " + message + "\n");
    }

    public void warn(String message) {
        output.append("WARN: " + message + "\n");
    }

    public void error(String message) {
        output.append("ERROR: " + message + "\n");
    }

    public void error(String message, Exception e) {
        output.append("ERROR: " + message + "\nCaused by: " + e);
    }
}
