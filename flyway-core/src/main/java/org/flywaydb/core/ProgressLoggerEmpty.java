package org.flywaydb.core;

public class ProgressLoggerEmpty implements ProgressLogger {
    @Override
    public ProgressLogger subTask(String operationName) {
        return this;
    }

    @Override
    public ProgressLogger pushSteps(int maxSteps) {
        return this;
    }

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, int step) {

    }
}