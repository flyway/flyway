package org.flywaydb.core.extensibility;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RootTelemetryModel {
    private String userId;
    private String sessionId;
    private String projectId;
    private String projectName;
    private String operationId;
    private String databaseEngine;
    private String databaseVersion;
    private String environment;
    private String applicationVersion;
    private String applicationEdition;
    private boolean redgateEmployee;

    private Instant startTime = Instant.now();
}