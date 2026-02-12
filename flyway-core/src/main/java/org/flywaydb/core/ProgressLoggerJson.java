/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.CustomLog;
import lombok.SneakyThrows;

@CustomLog
public class ProgressLoggerJson implements ProgressLogger {
    private final ProgressModel progressModel = new ProgressModel();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

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
