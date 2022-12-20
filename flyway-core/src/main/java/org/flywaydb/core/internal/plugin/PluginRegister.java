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
package org.flywaydb.core.internal.plugin;

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
    public final List<Plugin> REGISTERED_PLUGINS = new ArrayList<>();
    private final ClassLoader CLASS_LOADER = this.getClass().getClassLoader();
    private boolean hasRegisteredPlugins = false;

    public <T extends Plugin> T getPlugin(Class<T> clazz) {
        return (T) getPlugins()
                .stream()
                .filter(p -> p.getClass().getCanonicalName().equals(clazz.getCanonicalName()))
                .findFirst()
                .orElse(null);
    }

    public <T extends Plugin> List<T> getPlugins(Class<T> clazz) {
        return (List<T>) getPlugins()
                .stream()
                .filter(clazz::isInstance)
                .collect(Collectors.toList());
    }

    public <T extends Plugin> List<T> getLicensedPlugins(Class<T> clazz, Configuration configuration) {
        return (List<T>) getPlugins()
                .stream()
                .filter(clazz::isInstance)
                .filter(p -> p.isLicensed(configuration))
                .collect(Collectors.toList());
    }

    private List<Plugin> getPlugins() {
        registerPlugins();
        return REGISTERED_PLUGINS;
    }

    private void registerPlugins() {
        synchronized (REGISTERED_PLUGINS) {
            if (hasRegisteredPlugins) {
                return;
            }

            for (Plugin plugin : ServiceLoader.load(Plugin.class, CLASS_LOADER)) {
                REGISTERED_PLUGINS.add(plugin);
            }





            hasRegisteredPlugins = true;
        }
    }
}