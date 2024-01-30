package org.flywaydb.core.internal.command.clean;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Getter
@Setter
public class CleanModeConfigurationExtension implements ConfigurationExtension {
    public enum Mode {
        DEFAULT, SCHEMA, ALL;
    }

    @Deprecated
    CleanModel clean;

    @Override
    public String getNamespace() {
        return "plugins";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        return null;
    }
}