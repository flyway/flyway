/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util;

/**
 * Formats execution times.
 */
public class TimeFormat {
    /**
     * Prevent instantiation.
     */
    private TimeFormat() {
        // Do nothing
    }

    /**
     * Formats this execution time.
     *
     * @param millis The number of millis.
     * @return The execution in a human-readable format.
     */
    public static String format(long millis) {
        return String.format("%02d:%02d.%03ds", millis / 60000, (millis % 60000) / 1000, (millis % 1000));
    }
}
