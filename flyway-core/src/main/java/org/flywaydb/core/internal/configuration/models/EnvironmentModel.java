/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.internal.configuration.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.internal.util.MergeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ExtensionMethod(MergeUtils.class)
public class EnvironmentModel {

    private String url;
    private String user;
    private String password;
    private String driver;
    private List<String> schemas = new ArrayList<>();
    private List<String> jarDirs = new ArrayList<>();
    private String token;
    private Integer connectRetries = 0;
    private Integer connectRetriesInterval = 120;
    private String initSql;
    private Map<String, String> jdbcProperties;
    private Map<String, Map<String, Object>> resolvers;
    private String provisioner;
    public EnvironmentModel merge(EnvironmentModel otherPojo) {
        EnvironmentModel result = new EnvironmentModel();
        result.url = url.merge(otherPojo.url);
        result.user = user.merge(otherPojo.user);
        result.password = password.merge(otherPojo.password);
        result.driver = driver.merge(otherPojo.driver);
        result.schemas = schemas.merge(otherPojo.schemas);
        result.jarDirs = jarDirs.merge(otherPojo.jarDirs);
        result.token = token.merge(otherPojo.token);
        result.connectRetries = connectRetries.merge(otherPojo.connectRetries);
        result.connectRetriesInterval = connectRetriesInterval.merge(otherPojo.connectRetriesInterval);
        result.initSql = initSql.merge(otherPojo.initSql);
        result.jdbcProperties = MergeUtils.merge(jdbcProperties, otherPojo.jdbcProperties, MergeUtils::merge);
        result.resolvers = MergeUtils.merge(resolvers, otherPojo.resolvers, EnvironmentModel::MergeResolvers);
        result.provisioner = provisioner.merge(otherPojo.provisioner);
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