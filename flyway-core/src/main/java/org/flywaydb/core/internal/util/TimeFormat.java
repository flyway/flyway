package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Formats execution times.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeFormat {
    /**
     * Formats this execution time as minutes:seconds.millis. Ex.: 02:15.123s
     *
     * @param millis The number of millis.
     * @return The execution in a human-readable format.
     */
    public static String format(long millis) {
        return String.format("%02d:%02d.%03ds", millis / 60000, (millis % 60000) / 1000, (millis % 1000));
    }
}