package org.flywaydb.core.internal.util;

import java.util.concurrent.TimeUnit;

/**
 * Stop watch, inspired by the implementation in the Spring framework.
 */
public class StopWatch {
    private long start;
    private long stop;

    public void start() {
        start = nanoTime();
    }

    public void stop() {
        stop = nanoTime();
    }

    private long nanoTime() {
        return System.nanoTime();
    }

    /**
     * @return The total run time in millis of the stop watch between start and stop calls.
     */
    public long getTotalTimeMillis() {
        long duration = stop - start;
        return TimeUnit.NANOSECONDS.toMillis(duration);
    }
}