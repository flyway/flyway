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
package org.flywaydb.core.internal.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private final List<ServiceLoader.Provider<Plugin>> REGISTERED_PROVIDERS = new ArrayList<>();
    private final Map<ServiceLoader.Provider<Plugin>, Plugin> INSTANTIATED_PLUGINS = new ConcurrentHashMap<>();
    private final ClassLoader CLASS_LOADER = this.getClass().getClassLoader();
    private boolean hasRegisteredPlugins;

    /**
     * @deprecated Use {@link #getExact(Class)} instead.
     */
    @Deprecated
    public <T extends Plugin> T getPlugin(final Class<T> clazz) {
        return getExact(clazz);
    }

    /**
     * @deprecated Use {@link #getInstancesOf(Class)} instead.
     */
    @Deprecated
    public <T extends Plugin> List<T> getPlugins(final Class<T> clazz) {
        return getInstancesOf(clazz);
    }

    /**
     * @deprecated Use {@link #getLicensedInstancesOf(Class, Configuration)} instead.
     */
    @Deprecated
    public <T extends Plugin> List<T> getLicensedPlugins(final Class<T> clazz, final Configuration configuration) {
        return getLicensedInstancesOf(clazz, configuration);
    }

    /**
     * @deprecated Use {@link #getLicensedInstanceOf(Class, Configuration)} instead.
     */
    @Deprecated
    public <T extends Plugin> T getLicensedPlugin(final Class<T> clazz, final Configuration configuration) {
        return getLicensedInstanceOf(clazz, configuration);
    }

    /**
     * @deprecated Use {@link #getLicensedExact(String, Configuration)} instead.
     */
    @Deprecated
    public <T extends Plugin> T getLicensedPlugin(final String className, final Configuration configuration) {
        return getLicensedExact(className, configuration);
    }

    /**
     * @deprecated Use {@link #getExact(String)} instead.
     */
    @Deprecated
    public <T extends Plugin> T getPlugin(final String className) {
        return getExact(className);
    }

    /**
     * @deprecated Use {@link #getInstanceOf(Class)} instead.
     */
    @Deprecated
    public <T extends Plugin> T getPluginInstanceOf(final Class<T> clazz) {
        return getInstanceOf(clazz);
    }

    public <T extends Plugin> T getExact(final Class<T> clazz) {
        return (T) getMatchingProviders(clazz)
                .stream()
                .map(this::instantiate)
                .filter(p -> p != null && p.getClass().getCanonicalName().equals(clazz.getCanonicalName()))
                .findFirst()
                .orElse(null);
    }

    public <T extends Plugin> List<T> getInstancesOf(final Class<T> clazz) {
        return (List<T>) getMatchingProviders(clazz)
                .stream()
                .map(this::instantiate)
                .filter(p -> p != null && clazz.isInstance(p))
                .sorted()
                .collect(Collectors.toList());
    }

    public <T extends Plugin> List<T> getLicensedInstancesOf(final Class<T> clazz, final Configuration configuration) {
        return (List<T>) getMatchingProviders(clazz)
                .stream()
                .map(this::instantiate)
                .filter(p -> p != null && clazz.isInstance(p))
                .filter(p -> p.isLicensed(configuration))
                .sorted()
                .collect(Collectors.toList());
    }

    public <T extends Plugin> T getLicensedInstanceOf(final Class<T> clazz, final Configuration configuration) {
        return getLicensedInstancesOf(clazz, configuration).stream().findFirst().orElse(null);
    }

    public <T extends Plugin> T getLicensedExact(final String className, final Configuration configuration) {
        return (T) getProviders()
                .stream()
                .filter(p -> p.type().getSimpleName().equals(className))
                .map(this::instantiate)
                .filter(p -> p != null && p.isLicensed(configuration))
                .findFirst()
                .orElse(null);
    }

    public <T extends Plugin> T getExact(final String className) {
        return (T) getProviders()
            .stream()
            .filter(p -> p.type().getSimpleName().equals(className))
            .map(this::instantiate)
            .filter(p -> p != null)
            .findFirst()
            .orElse(null);
    }

    public <T extends Plugin> T getInstanceOf(final Class<T> clazz) {
        return (T) getMatchingProviders(clazz)
            .stream()
            .map(this::instantiate)
            .filter(p -> p != null && clazz.isInstance(p))
            .sorted()
            .findFirst()
            .orElse(null);
    }

    private Plugin instantiate(final ServiceLoader.Provider<Plugin> provider) {
        return INSTANTIATED_PLUGINS.computeIfAbsent(provider, p -> {
            final Plugin plugin = p.get();
            if (plugin.isEnabled()) {
                return plugin;
            }
            return null;
        });
    }

    private List<ServiceLoader.Provider<Plugin>> getProviders() {
        registerPlugins();
        return Collections.unmodifiableList(REGISTERED_PROVIDERS);
    }

    private <T extends Plugin> List<ServiceLoader.Provider<Plugin>> getMatchingProviders(final Class<T> clazz) {
        return getProviders()
            .stream()
            .filter(p -> clazz.isAssignableFrom(p.type()))
            .collect(Collectors.toList());
    }

    void registerPlugins() {
        synchronized (REGISTERED_PROVIDERS) {
            if (hasRegisteredPlugins) {
                return;
            }

            ServiceLoader.load(Plugin.class, CLASS_LOADER)
                .stream()
                .forEach(REGISTERED_PROVIDERS::add);

            hasRegisteredPlugins = true;
        }
    }

    public PluginRegister getCopy() {
        final PluginRegister copy = new PluginRegister();
        copy.REGISTERED_PROVIDERS.clear();
        copy.REGISTERED_PROVIDERS.addAll(getProviders());
        // Copy already-instantiated plugins. Plugins not yet instantiated will be
        // created fresh from providers when first accessed on the copy, ensuring
        // independent state between PluginRegister instances.
        for (final var entry : INSTANTIATED_PLUGINS.entrySet()) {
            if (entry.getValue() != null) {
                copy.INSTANTIATED_PLUGINS.put(entry.getKey(), entry.getValue().copy());
            }
        }
        copy.hasRegisteredPlugins = true;
        return copy;
    }
}
