/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import java.util.Collection;

/**
 * Resolves available migrations. This interface can be implemented to create custom resolvers. A custom resolver
 * can be used to create additional types of migrations not covered by the standard resolvers (jdbc, sql, spring-jdbc).
 * Using the {@link org.flywaydb.core.Flyway#setSkipDefaultResolvers(boolean)}, the built-in resolvers can also be
 * completely replaced.
 *
 * <p>If a resolver also implements the {@link org.flywaydb.core.api.configuration.ConfigurationAware} interface,
 * a {@link org.flywaydb.core.api.configuration.FlywayConfiguration} object will automatically be injected before
 * calling {@link #resolveMigrations()}, giving the resolver access to the core flyway configuration, which provides
 * useful data like resolve locations or placeholder configuration.</p>
 *
 * An abstract implementation is provided in {@link BaseMigrationResolver} which handles the storing of the
 * configuration. It is encouraged to subclass that class instead of implementing this interface directly.
 */
public interface MigrationResolver {
    /**
     * Resolves the available migrations.
     *
     * @return The available migrations.
     */
    Collection<ResolvedMigration> resolveMigrations();
}
