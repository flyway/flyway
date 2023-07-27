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
package org.flywaydb.core.api.migration.baseline;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.ConfigUtils;

import java.util.Map;

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