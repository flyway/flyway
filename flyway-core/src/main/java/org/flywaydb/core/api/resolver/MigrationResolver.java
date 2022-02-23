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
package org.flywaydb.core.api.resolver;

import org.flywaydb.core.internal.util.Pair;

import java.util.Collection;
import java.util.List;

/**
 * Resolves available migrations. This interface can be implemented to create custom resolvers. A custom resolver
 * can be used to create additional types of migrations not covered by the standard resolvers (jdbc, sql, spring-jdbc).
 * Using the skipDefaultResolvers configuration property, the built-in resolvers can also be completely replaced.
 */
public interface MigrationResolver {
    /**
     * Resolves the available migrations.
     *
     * @return The available migrations.
     */
    Pair<List<ResolvedMigration>, Collection<UnresolvedMigration>> attemptResolveMigrations(Context context);
}