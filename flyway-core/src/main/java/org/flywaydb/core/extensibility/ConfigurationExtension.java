/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.core.extensibility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.flywaydb.core.api.FlywayException;

import java.util.Map;

public interface ConfigurationExtension extends Plugin {
    @JsonIgnore
    String getNamespace();
    @Deprecated
    default void extractParametersFromConfiguration(Map<String, String> configuration) {
        // Do nothing
    }
    String getConfigurationParameterFromEnvironmentVariable(String environmentVariable);

    @Override
    default Plugin copy() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(this), this.getClass());
        }
        catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @JsonIgnore
    default boolean isStub() {
        return false;
    }
}
