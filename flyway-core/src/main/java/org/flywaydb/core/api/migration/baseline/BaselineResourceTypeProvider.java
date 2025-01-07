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
package org.flywaydb.core.api.migration.baseline;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ResourceType;
import org.flywaydb.core.extensibility.ResourceTypeProvider;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BaselineResourceTypeProvider implements ResourceTypeProvider {
    private enum BaselineResourceType implements ResourceType {
        BASELINE_MIGRATION;

        @Override
        public boolean isVersioned() {
            return true;
        }
    }

    @Override
    public List<Pair<String, ResourceType>> getPrefixTypePairs(Configuration configuration) {
        List<Pair<String, ResourceType>> pairs = new ArrayList<>();
        pairs.add(Pair.of(configuration.getPluginRegister().getPlugin(BaselineMigrationConfigurationExtension.class).getBaselineMigrationPrefix(), BaselineResourceType.BASELINE_MIGRATION));
        return pairs;
    }
}
