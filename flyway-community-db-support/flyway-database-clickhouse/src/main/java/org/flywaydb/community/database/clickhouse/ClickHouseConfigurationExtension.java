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
package org.flywaydb.community.database.clickhouse;

import lombok.Getter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

import java.util.Map;

@Getter
public class ClickHouseConfigurationExtension implements ConfigurationExtension {
    private static final String CLUSTER_NAME = "flyway.clickhouse.clusterName";
    private static final String ZOOKEEPER_PATH = "flyway.clickhouse.zookeeperPath";

    private static final String ZOOKEEPER_PATH_DEFAULT_VALUE = "/clickhouse/tables/{shard}/{database}/{table}";

    private String clusterName;
    private String zookeeperPath = ZOOKEEPER_PATH_DEFAULT_VALUE;

    @Override
    public String getNamespace() {
        return "clickhouse";
    }

    @Override
    public void extractParametersFromConfiguration(Map<String, String> configuration) {
        String clusterName = configuration.remove(CLUSTER_NAME);
        if (clusterName != null) {
            this.clusterName = clusterName;
        }

        String zookeeperPath = configuration.remove(ZOOKEEPER_PATH);
        if (zookeeperPath != null) {
            this.zookeeperPath = zookeeperPath;
        }
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_CLICKHOUSE_CLUSTER_NAME".equals(environmentVariable)) {
            return CLUSTER_NAME;
        }
        if ("FLYWAY_CLICKHOUSE_ZOOKEEPER_PATH".equals(environmentVariable)) {
            return ZOOKEEPER_PATH;
        }
        return null;
    }
}
