/*-
 * ========================LICENSE_START=================================
 * flyway-gradle-plugin
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
package org.flywaydb.gradle.task;

import java.util.Map;
import java.util.stream.Collectors;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

public abstract class FlywayEnvVarsValueSource implements ValueSource<Map<String, String>, ValueSourceParameters.None> {

    private static final String FLYWAY_ENV_PREFIX = "FLYWAY_";

    @Override
    public Map<String, String> obtain() {
        return System.getenv()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(FLYWAY_ENV_PREFIX))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
