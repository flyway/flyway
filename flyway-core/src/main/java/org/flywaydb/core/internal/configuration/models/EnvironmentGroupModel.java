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
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;

@Getter
@Setter
@NoArgsConstructor
public class EnvironmentGroupModel {
    private String displayName;

    @JsonAnySetter
    @Getter(onMethod = @__(@ClassUtils.DoNotMapForLogging))
    private Map<String, Object> unknownConfigurations = new HashMap<>();

    public EnvironmentGroupModel merge(final EnvironmentGroupModel otherPojo) {
        final EnvironmentGroupModel result = new EnvironmentGroupModel();
        result.displayName = MergeUtils.merge(displayName, otherPojo.displayName);
        result.unknownConfigurations = MergeUtils.merge(unknownConfigurations,
            otherPojo.unknownConfigurations,
            MergeUtils::mergeObjects);
        return result;
    }
}
