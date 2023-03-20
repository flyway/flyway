/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.extensibility;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.FlywayTelemetryManager;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
public class EventTelemetryModel implements AutoCloseable {
    private String name;
    private long duration;
    private Exception exception;

    private final FlywayTelemetryManager flywayTelemetryManager;
    private Instant startTime;
    public EventTelemetryModel(String name, FlywayTelemetryManager flywayTelemetryManager) {
        startTime = Instant.now();
        this.flywayTelemetryManager = flywayTelemetryManager;
        this.name = name;

    }
    @Override
    public void close() throws Exception {
        duration = Duration.between(startTime, Instant.now()).toMillis();
        if(flywayTelemetryManager != null) {
            flywayTelemetryManager.LogEvent(this);
        }
    }
}