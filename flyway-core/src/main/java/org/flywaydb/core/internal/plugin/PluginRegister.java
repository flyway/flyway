/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.ConfigurationProvider;
import org.flywaydb.core.extensibility.PluginMetadata;
import org.flywaydb.core.internal.database.DatabaseType;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

@SuppressWarnings("rawtypes")
@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginRegister {

    private static final ClassLoader CLASS_LOADER = new PluginRegister().getClass().getClassLoader();
    private static final List<PluginMetadata> REGISTERED_PLUGIN_METADATA = new ArrayList<>();
    private static final List<DatabaseType> REGISTERED_DATABASE_TYPES = new ArrayList<>();
    private static final List<ConfigurationProvider> REGISTERED_CONFIGURATION_PROVIDERS = new ArrayList<>();
    private static final List<ConfigurationExtension> REGISTERED_CONFIGURATION_EXTENSIONS = new ArrayList<>();
    private static boolean hasRegisteredPlugins = false;

    public static void registerPlugins() {
        synchronized (REGISTERED_DATABASE_TYPES) {
            if (hasRegisteredPlugins) {
                return;
            }

            for(PluginMetadata pluginMetadata : ServiceLoader.load(PluginMetadata.class, CLASS_LOADER)) {
                REGISTERED_PLUGIN_METADATA.add(pluginMetadata);
                LOG.debug("Adding Plugin: " + pluginMetadata.getClass().getName());
            }

            for(DatabaseType databaseTypePlugin : ServiceLoader.load(DatabaseType.class, CLASS_LOADER)) {
                REGISTERED_DATABASE_TYPES.add(databaseTypePlugin);
                LOG.debug("Adding DB: " + databaseTypePlugin.getClass().getName());
            }



            Collections.sort(REGISTERED_DATABASE_TYPES);

            for(ConfigurationProvider configurationProviderPlugin : ServiceLoader.load(ConfigurationProvider.class, CLASS_LOADER)) {
                REGISTERED_CONFIGURATION_PROVIDERS.add(configurationProviderPlugin);
                LOG.debug("Adding ConfigurationProvider: " + configurationProviderPlugin.getClass().getName());
            }

            for(ConfigurationExtension configurationExtensionPlugin : ServiceLoader.load(ConfigurationExtension.class, CLASS_LOADER)) {
                REGISTERED_CONFIGURATION_EXTENSIONS.add(configurationExtensionPlugin);
                LOG.debug("Adding ConfigurationExtension: " + configurationExtensionPlugin.getClass().getName());
            }

            hasRegisteredPlugins = true;
        }
    }

    public static List<PluginMetadata> getFlywayExtensions() {
        if (!hasRegisteredPlugins) {
            registerPlugins();
        }
        return REGISTERED_PLUGIN_METADATA;
    }

    public static List<DatabaseType> getDatabaseTypes() {
        if (!hasRegisteredPlugins) {
            registerPlugins();
        }
        return REGISTERED_DATABASE_TYPES;
    }

    public static List<ConfigurationProvider> getConfigurationProviders() {
        if (!hasRegisteredPlugins) {
            registerPlugins();
        }
        return REGISTERED_CONFIGURATION_PROVIDERS;
    }

    public static List<ConfigurationExtension> getConfigurationExtensions() {
        if (!hasRegisteredPlugins) {
            registerPlugins();
        }
        return REGISTERED_CONFIGURATION_EXTENSIONS;
    }

    public static <T extends ConfigurationExtension> T getConfigurationExtension(Class<T> clazz) {
        for (ConfigurationExtension configurationExtension : REGISTERED_CONFIGURATION_EXTENSIONS) {
            if (clazz.isInstance(configurationExtension)) {
                return (T) configurationExtension;
            }
        }

        throw new FlywayException("Requested configuration extension of type " + clazz.getName() + " but none found.");
    }
}