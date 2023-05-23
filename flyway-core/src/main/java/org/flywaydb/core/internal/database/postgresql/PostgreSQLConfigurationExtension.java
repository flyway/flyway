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
package org.flywaydb.core.internal.database.postgresql;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

import java.util.Map;

@Getter
@Setter
public class PostgreSQLConfigurationExtension implements ConfigurationExtension {
    private static final String TRANSACTIONAL_LOCK = "flyway.postgresql.transactional.lock";

    private boolean transactionalLock = true;

    @Override
    public void extractParametersFromConfiguration(Map<String, String> configuration) {
        transactionalLock = Boolean.parseBoolean(configuration.getOrDefault(TRANSACTIONAL_LOCK, Boolean.toString(transactionalLock)));
        configuration.remove(TRANSACTIONAL_LOCK);
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK".equals(environmentVariable)) {
            return TRANSACTIONAL_LOCK;
        }
        return null;
    }
}