package org.flywaydb.community.database.clickhouse;

import lombok.Getter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

import java.util.Map;

@Getter
public class ClickHouseConfigurationExtension implements ConfigurationExtension {
    private static final String CLUSTER_NAME = "flyway.clickhouse.clusterName";

    private String clusterName;

    @Override
    public void extractParametersFromConfiguration(Map<String, String> configuration) {
        String clusterName = configuration.remove(CLUSTER_NAME);
        if (clusterName != null) {
            this.clusterName = clusterName;
        }
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_CLICKHOUSE_CLUSTER_NAME".equals(environmentVariable)) {
            return CLUSTER_NAME;
        }
        return null;
    }
}
