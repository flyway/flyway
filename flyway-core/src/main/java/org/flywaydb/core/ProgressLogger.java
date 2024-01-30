package org.flywaydb.core;

public interface ProgressLogger {
    ProgressLogger subTask(String operationName);

    ProgressLogger pushSteps(int steps);

    void log(String message);

    void log(String message, int step);
}
