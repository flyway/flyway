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
package org.flywaydb.core.experimental;

import java.util.Comparator;
import java.util.Optional;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.Pair;

public class ExperimentalDatabasePluginResolverImpl implements ExperimentalDatabasePluginResolver{

    private final PluginRegister pluginRegister;

    public ExperimentalDatabasePluginResolverImpl(final PluginRegister pluginRegister) {
        this.pluginRegister = pluginRegister;
    }

    @Override
    public Optional<ExperimentalDatabase> resolve(final String url) {
        return pluginRegister.getPlugins(ExperimentalDatabase.class)
                             .stream()
                             .map(p -> Pair.of(p.supportsUrl(url), p))
                             .filter(p -> p.getLeft().isSupported())
                             .max(Comparator.comparingInt(p -> p.getLeft().priority()))
                             .map(Pair::getRight);
    }
}
