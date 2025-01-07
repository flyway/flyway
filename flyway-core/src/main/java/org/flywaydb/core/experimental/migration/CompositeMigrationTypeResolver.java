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
package org.flywaydb.core.experimental.migration;

import java.util.List;
import java.util.Objects;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.MigrationType;

public class CompositeMigrationTypeResolver implements MigrationTypeResolver {
    @Override
    public MigrationType resolveMigrationType(final String filename, final Configuration configuration) {
        final List<MigrationTypeResolver> plugins = configuration.getPluginRegister().getPlugins(MigrationTypeResolver.class);
        return plugins
            .stream()
            .map(plugin -> plugin.resolveMigrationType(filename, configuration))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public MigrationType resolveMigrationTypeFromName(final String name, final Configuration configuration) {
        final List<MigrationTypeResolver> plugins = configuration.getPluginRegister().getPlugins(MigrationTypeResolver.class);
        return plugins
            .stream()
            .map(plugin -> {
                try {
                    return plugin.resolveMigrationTypeFromName(name, configuration);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
