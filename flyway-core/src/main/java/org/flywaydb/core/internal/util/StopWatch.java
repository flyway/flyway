/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util;

import java.util.concurrent.TimeUnit;

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
        start = System.nanoTime();
    }

    /**
     * Stops the stop watch.
     */
    public void stop() {
        stop = System.nanoTime();
    }

    /**
     * @return The total run time in millis of the stop watch between start and stop calls.
     */
    public long getTotalTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(stop - start);
    }
}
