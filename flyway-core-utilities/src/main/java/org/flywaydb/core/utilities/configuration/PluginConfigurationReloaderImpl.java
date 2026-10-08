/*-
 * ========================LICENSE_START=================================
 * flyway-core-utilities
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
package org.flywaydb.core.utilities.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.PluginConfigurationReloader;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.FlywayEnvironmentModel;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.MergeUtils;

public class PluginConfigurationReloaderImpl implements PluginConfigurationReloader {

    @Override
    public void reload(final ConfigurationModel model,
        final PluginRegister pluginRegister,
        final String environmentName) {
        final PluginRegister boundFromScratch = new PluginRegister();
        ConfigurationExtensionBinder.bind(model, environmentName, boundFromScratch);

        final Map<String, Object> allSettings = Stream.concat(Stream.of(model.getFlyway()),
                model.getEnvironments().values().stream().map(EnvironmentModel::getFlyway))
            .map(FlywayEnvironmentModel::getPluginConfigurations)
            .reduce(Map.of(), (a, b) -> MergeUtils.merge(a, b, MergeUtils::mergeObjects));

        for (final ConfigurationExtension extension : pluginRegister.getInstancesOf(ConfigurationExtension.class)) {
            if (!extension.isStub() && !extension.getNamespace().startsWith("\\")) {
                copyConfigurableFields(boundFromScratch.getExact(extension.getClass()), extension, allSettings);
            }
        }
    }

    private static void copyConfigurableFields(final ConfigurationExtension source,
        final ConfigurationExtension target,
        final Map<String, Object> allSettings) {
        final String namespace = target.getNamespace();
        if (!((namespace.isEmpty() ? allSettings : allSettings.get(namespace)) instanceof final Map<?, ?> settings)) {
            return;
        }

        try {
            for (final Field field : Arrays.stream(target.getClass().getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))
                .filter(field -> settings.keySet()
                    .stream()
                    .anyMatch(key -> field.getName().equalsIgnoreCase(String.valueOf(key))))
                .toList()) {
                field.setAccessible(true);
                field.set(target, field.get(source));
            }
        } catch (final IllegalAccessException e) {
            throw new FlywayException(e);
        }
    }
}
