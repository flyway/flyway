package org.flywaydb.core;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.CustomLog;
import lombok.SneakyThrows;

@CustomLog
public class ProgressLoggerJson implements ProgressLogger {
    private final ProgressModel progressModel = new ProgressModel();
    private final JsonMapper jsonMapper = new JsonMapper();

    public ProgressLogger subTask(String operationName) {
        return new ProgressLoggerJson(this.progressModel.getOperation() + "." + operationName);
    }

    public ProgressLoggerJson(String operationName) {
        progressModel.setOperation(operationName);
    }

    @SneakyThrows
    public void log(String message) {
        progressModel.setMessage(message);
        System.err.println(jsonMapper.writeValueAsString(progressModel));
        progressModel.setStepAndTotal(progressModel.getStep() + 1);
    }

    @SneakyThrows
    public void log(String message, int step) {
        progressModel.setMessage(message);
        progressModel.setStepAndTotal(step);
        System.err.println(jsonMapper.writeValueAsString(progressModel));
    }

    public ProgressLogger pushSteps(int steps) {
        progressModel.setTotalSteps(progressModel.getTotalSteps() == null ? steps : steps + progressModel.getTotalSteps());
        return this;
    }
}