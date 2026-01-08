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
import org.flywaydb.core.api.configuration.Configuration;

public interface Plugin extends Comparable<Plugin> {
    @JsonIgnore
    default boolean isLicensed(Configuration configuration) {
        return true;
    }
    @JsonIgnore
    default boolean isEnabled() {
        return true;
    }

    @JsonIgnore
    default String getName(){
        return this.getClass().getSimpleName();
    }

    @JsonIgnore
    default String getPluginVersion(Configuration config) {
        return null;
    }

    /**
     * High numbers indicate that this type will be used in preference to lower priorities.
     */
    @JsonIgnore
    default int getPriority() {
        return 0;
    }

    default int compareTo(Plugin o) {
        return o.getPriority() - getPriority();
    }

    default Plugin copy() {
        return this;
    }
}
