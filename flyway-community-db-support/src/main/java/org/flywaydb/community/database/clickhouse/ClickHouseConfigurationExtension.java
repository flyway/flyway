package org.flywaydb.community.database.clickhouse;

import org.flywaydb.core.extensibility.ConfigurationExtension;

import java.util.Map;

public class ClickHouseConfigurationExtension implements ConfigurationExtension {
    private static final String CLUSTER_NAME = "flyway.clickhouse.clusterName";

    private String clusterName;

    public String getClusterName() {
        return clusterName;
    }

    @Override
    public void extractParametersFromConfiguration(Map<String, String> configuration) {
        clusterName = configuration.get(CLUSTER_NAME);
        configuration.remove(CLUSTER_NAME);
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_CLICKHOUSE_CLUSTER_NAME".equals(environmentVariable)) {
            return CLUSTER_NAME;
        }
        return null;
    }
}
