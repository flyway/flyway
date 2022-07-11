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
package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ResourceType;
import org.flywaydb.core.extensibility.ResourceTypeProvider;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class CoreResourceTypeProvider implements ResourceTypeProvider {
    private enum CoreResourceType implements ResourceType {
        MIGRATION,



        REPEATABLE_MIGRATION,
        CALLBACK;

        /**
         * Whether the given resource type represents a resource that is versioned.
         */
        public boolean isVersioned() {
            return this == CoreResourceType.MIGRATION



                    ;
        }
    }

    @Override
    public List<Pair<String, ResourceType>> getPrefixTypePairs(Configuration configuration) {
        List<Pair<String, ResourceType>> pairs = new ArrayList<>();
        pairs.add(Pair.of(configuration.getSqlMigrationPrefix(), CoreResourceType.MIGRATION));



        pairs.add(Pair.of(configuration.getRepeatableSqlMigrationPrefix(), CoreResourceType.REPEATABLE_MIGRATION));

        for (Event event : Event.values()) {
            pairs.add(Pair.of(event.getId(), CoreResourceType.CALLBACK));
        }

        return pairs;
    }
}