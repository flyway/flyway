package org.flywaydb.core;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

@RequiredArgsConstructor
public class ProgressLoggerSynchronized implements ProgressLogger {

    private final ProgressLogger progressLogger;

    @Override
    @Synchronized
    public ProgressLogger subTask(String operationName) {
        return new ProgressLoggerSynchronized(progressLogger.subTask(operationName));
    }

    @Override
    @Synchronized
    public ProgressLogger pushSteps(int steps) {
        return new ProgressLoggerSynchronized(progressLogger.pushSteps(steps));
    }

    @Override
    @Synchronized
    public void log(String message) {
        progressLogger.log(message);
    }

    @Override
    @Synchronized
    public void log(String message, int step) {
        progressLogger.log(message, step);
    }
}