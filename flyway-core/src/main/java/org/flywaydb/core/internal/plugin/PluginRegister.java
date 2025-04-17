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
package org.flywaydb.core.internal.plugin;

import java.util.Collections;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@CustomLog
@NoArgsConstructor
public class PluginRegister {
    private final List<Plugin> REGISTERED_PLUGINS = new ArrayList<>();
    private final ClassLoader CLASS_LOADER = this.getClass().getClassLoader();
    private boolean hasRegisteredPlugins;

    public <T extends Plugin> T getPlugin(final Class<T> clazz) {
        return (T) getPlugins()
                .stream()
                .filter(p -> p.getClass().getCanonicalName().equals(clazz.getCanonicalName()))
                .findFirst()
                .orElse(null);
    }

    public <T extends Plugin> List<T> getPlugins(final Class<T> clazz) {
        return (List<T>) getPlugins()
                .stream()
                .filter(clazz::isInstance)
                .sorted()
                .collect(Collectors.toList());
    }

    public <T extends Plugin> List<T> getLicensedPlugins(final Class<T> clazz, final Configuration configuration) {
        return (List<T>) getPlugins()
                .stream()
                .filter(clazz::isInstance)
                .filter(p -> p.isLicensed(configuration))
                .sorted()
                .collect(Collectors.toList());
    }

    public <T extends Plugin> T getLicensedPlugin(final Class<T> clazz, final Configuration configuration) {
        return getLicensedPlugins(clazz, configuration).stream().findFirst().orElse(null);
    }

    public <T extends Plugin> T getLicensedPlugin(final String className, final Configuration configuration) {
        return (T) getPlugins()
                .stream()
                .filter(p -> p.isLicensed(configuration))
                .filter(p -> p.getClass().getSimpleName().equals(className))
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public <T extends Plugin> T getPlugin(final String className) {
        return (T) getPlugins()
            .stream()
            .filter(p -> p.getClass().getSimpleName().equals(className))
            .sorted()
            .findFirst()
            .orElse(null);
    }

    public <T extends Plugin> T getPluginInstanceOf(final Class<T> clazz) {
        return (T) getPlugins()
            .stream()
            .filter(clazz::isInstance)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    private List<Plugin> getPlugins() {
        registerPlugins();
        return Collections.unmodifiableList(REGISTERED_PLUGINS);
    }

    void registerPlugins() {
        synchronized (REGISTERED_PLUGINS) {
            if (hasRegisteredPlugins) {
                return;
            }

            for (final Plugin plugin : ServiceLoader.load(Plugin.class, CLASS_LOADER)) {
                if (plugin.isEnabled()) {
                    REGISTERED_PLUGINS.add(plugin);
                }
            }

            hasRegisteredPlugins = true;
        }
    }

    public PluginRegister getCopy(){
        final PluginRegister copy = new PluginRegister();
        copy.REGISTERED_PLUGINS.clear();
        copy.REGISTERED_PLUGINS.addAll(getPlugins().stream().map(Plugin::copy).toList());
        copy.hasRegisteredPlugins = true;
        return copy;
    }
}
