/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
import org.flywaydb.core.internal.util.MergeUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ConfigurationModel {
    private Map<String, EnvironmentModel> environments = new HashMap<>();
    private FlywayModel flyway = new FlywayModel();

    public static ConfigurationModel defaults() {
        ConfigurationModel model = new ConfigurationModel();
        model.flyway = FlywayModel.defaults();
        model.environments.put("default", new EnvironmentModel());
        return model;
    }

    public ConfigurationModel merge(ConfigurationModel otherPojo) {
        ConfigurationModel result = new ConfigurationModel();
        result.flyway = flyway != null ? flyway.merge(otherPojo.flyway) : otherPojo.flyway;
        result.environments = MergeUtils.merge(environments, otherPojo.environments, EnvironmentModel::merge);
        return result;
    }

    public static ConfigurationModel clone(ConfigurationModel pojo) {
        ConfigurationModel basePojo = new ConfigurationModel();
        return basePojo.merge(pojo);
    }
}