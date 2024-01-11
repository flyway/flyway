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
            flywayTelemetryManager.logEvent(this);
        }
    }
}