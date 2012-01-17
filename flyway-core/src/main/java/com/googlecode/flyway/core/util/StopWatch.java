package com.googlecode.flyway.core.util;

/**
 * Stop watch, inspired by the implementation in the Spring framework.
 */
public class StopWatch {
    /**
     * The timestamp at which the stopwatch was started.
     */
    private long start;

    /**
     * The timestamp at which the stopwatch was stopped.
     */
    private long stop;

    /**
     * Starts the stop watch.
     */
    public void start() {
        start = System.currentTimeMillis();
    }

    /**
     * Stops the stop watch.
     */
    public void stop() {
        stop = System.currentTimeMillis();
    }

    /**
     * @return The total run time in millis of the stop watch between start and stop calls.
     */
    public long getTotalTimeMillis() {
        return stop - start;
    }
}
