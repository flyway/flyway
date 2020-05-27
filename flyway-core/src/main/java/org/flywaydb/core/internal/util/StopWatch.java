/*
 * Copyright 2010-2020 Redgate Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        start = nanoTime();
    }

    /**
     * Stops the stop watch.
     */
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