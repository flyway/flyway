package org.flywaydb.core.extensibility;

public interface TelemetryPlugin extends Plugin, AutoCloseable {
    void logRootDetails(RootTelemetryModel rootTelemetryModel);
    void logEventDetails(EventTelemetryModel eventModel);
}