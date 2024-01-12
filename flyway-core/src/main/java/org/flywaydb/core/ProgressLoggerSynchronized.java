/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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