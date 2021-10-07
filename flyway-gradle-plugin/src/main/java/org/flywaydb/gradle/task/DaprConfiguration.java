/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.gradle.task;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.internal.configuration.ConfigUtils;

import java.util.Map;

@Getter
@Setter
public class DaprConfiguration {
    public static final String DAPR_URL = "flyway.dapr.url";
    public static final String DAPR_SECRETS = "flyway.dapr.secrets";

    private String daprUrl;
    private String[] daprSecrets;

    public void extract(Map<String, String> conf) {
        ConfigUtils.putIfSet(conf, DAPR_URL, daprUrl);
        ConfigUtils.putArrayIfSet(conf, DAPR_SECRETS, daprSecrets);
    }
}