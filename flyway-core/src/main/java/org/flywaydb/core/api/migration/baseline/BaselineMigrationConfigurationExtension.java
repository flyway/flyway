package org.flywaydb.core.api.migration.baseline;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Getter
@Setter
public class BaselineMigrationConfigurationExtension implements ConfigurationExtension {

    private static final String BASELINE_MIGRATION_PREFIX = "flyway.baselineMigrationPrefix";
    private String baselineMigrationPrefix = "B";

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_BASELINE_MIGRATION_PREFIX".equals(environmentVariable)) {
            return BASELINE_MIGRATION_PREFIX;
        }
        return null;
    }
}