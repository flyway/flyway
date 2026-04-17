/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.configuration.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class EnvironmentModel {

    private String url;
    private String user;
    private String password;
    private String driver;
    private List<String> schemas = new ArrayList<>();
    private Integer connectRetries;
    private Integer connectRetriesInterval;
    private String initSql;
    private Map<String, String> jdbcProperties = new HashMap<>();
    private Map<String, Map<String, Object>> resolvers;
    private String provisioner;
    private FlywayEnvironmentModel flyway = new FlywayEnvironmentModel();

    @JsonAnySetter
    @Getter(onMethod = @__(@ClassUtils.DoNotMapForLogging))
    private Map<String,Object> unknownConfigurations = new HashMap<>();

    public EnvironmentModel merge(EnvironmentModel otherPojo) {
        EnvironmentModel result = new EnvironmentModel();
        result.url = MergeUtils.merge(url, otherPojo.url);
        result.user = MergeUtils.merge(user, otherPojo.user);
        result.password = MergeUtils.merge(password, otherPojo.password);
        result.driver = MergeUtils.merge(driver, otherPojo.driver);
        result.schemas = MergeUtils.merge(schemas, otherPojo.schemas);
        result.connectRetries = MergeUtils.merge(connectRetries, otherPojo.connectRetries);
        result.connectRetriesInterval = MergeUtils.merge(connectRetriesInterval, otherPojo.connectRetriesInterval);
        result.initSql = MergeUtils.merge(initSql, otherPojo.initSql);
        result.jdbcProperties = MergeUtils.merge(jdbcProperties, otherPojo.jdbcProperties, MergeUtils::merge);
        result.resolvers = MergeUtils.merge(resolvers, otherPojo.resolvers, EnvironmentModel::MergeResolvers);
        result.provisioner = MergeUtils.merge(provisioner, otherPojo.provisioner);
        result.flyway = flyway.merge(otherPojo.flyway);
        result.unknownConfigurations = MergeUtils.merge(unknownConfigurations, otherPojo.unknownConfigurations, MergeUtils::mergeObjects);
        return result;
    }

    private static Map<String, Object> MergeResolvers(Map<String, Object> primary, Map<String, Object> overrides) {
        if(primary == null) {
            return overrides;
        }
        if(overrides == null) {
            return primary;
        }
        return MergeUtils.merge(primary, overrides, MergeUtils::merge);
    }
}
