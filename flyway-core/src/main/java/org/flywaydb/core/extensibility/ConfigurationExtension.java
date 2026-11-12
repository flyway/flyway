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
package org.flywaydb.core.extensibility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;

import java.util.Map;

public interface ConfigurationExtension extends Plugin {
    boolean JACKSON_DATABIND_PRESENT = ClassUtils.isPresent("tools.jackson.databind.ObjectMapper",
        ConfigurationExtension.class.getClassLoader());

    @JsonIgnore
    String getNamespace();

    @Deprecated
    default void extractParametersFromConfiguration(final Map<String, String> configuration) {
        // Do nothing
    }

    default String getConfigurationParameterFromEnvironmentVariable(final String environmentVariable) {
        return null;
    }

    /**
     * A copy of this instance. With jackson-databind present, cloned via a JSON round-trip, so
     * nested mutable state is independent of the source - but copying goes through bean
     * getters/setters, so derived getters, {@code @JsonIgnore} fields, and {@code final} fields
     * can end up copied differently than a raw field copy would. Without jackson-databind, falls
     * back to a reflection-based shallow copy of declared fields (skipping {@code final} ones) -
     * nested mutable state is shared with the source instead. The two paths aren't guaranteed to
     * match beyond plain, unannotated fields.
     */
    @Override
    default Plugin copy() {
        if (JACKSON_DATABIND_PRESENT) {
            final ObjectMapper objectMapper = ConfigurationExtensionCopyMapper.INSTANCE;
            try {
                return objectMapper.readValue(objectMapper.writeValueAsString(this), this.getClass());
            } catch (final Exception e) {
                throw new FlywayException(e);
            }
        }

        try {
            final ConfigurationExtension target = getClass().getDeclaredConstructor()
                .newInstance();
            MergeUtils.copyModel(this, target);
            return target;
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @JsonIgnore
    default boolean isStub() {
        return false;
    }
}

final class ConfigurationExtensionCopyMapper {
    static final ObjectMapper INSTANCE = new ObjectMapper();

    private ConfigurationExtensionCopyMapper() {
    }
}
