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
package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

@SuppressWarnings("unused")
public interface EnvironmentProvisioner extends Plugin {
    String getName();

    default Class<?> getConfigClass() {
        return null;
    }

    default void setConfiguration(final ConfigurationExtension config) {}

    default void preProvision(final PropertyResolverContext context, final ProgressLogger progress) {}

    default void preReprovision(final PropertyResolverContext context, final ProgressLogger progress) {}

    default void postProvision(final PropertyResolverContext context,
        final ResolvedEnvironment resolvedEnvironment,
        final ProgressLogger progress) {}

    default void postReprovision(final PropertyResolverContext context,
        final ResolvedEnvironment resolvedEnvironment,
        final ProgressLogger progress) {}
}
