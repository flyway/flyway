package org.flywaydb.core.extensibility;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.FlywayTelemetryManager;

@Getter
@Setter
public class InfoTelemetryModel extends EventTelemetryModel {
    private int numberOfMigrations;
    private int numberOfPendingMigrations;
    private String oldestMigrationInstalledOnUTC;

    public InfoTelemetryModel(FlywayTelemetryManager flywayTelemetryManager){
        super("info", flywayTelemetryManager);
    }
}