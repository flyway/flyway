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

import static org.flywaydb.core.internal.util.ExceptionUtils.getFlywayExceptionMessage;
import static org.flywaydb.core.internal.util.ExceptionUtils.getRootCause;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.MergeUtils;
import org.flywaydb.core.internal.util.StringUtils;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Binds configuration extensions from the {@code flyway} namespace, layering the given environment's
 * {@code environments.<name>.flyway} values over the root values key by key.
 */
@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationExtensionBinder {

    private static final Pattern ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN = Pattern.compile("\\[\"([^\"]*)\"]");
    private static final String UNABLE_TO_PARSE_FIELD = "Unable to parse parameter '%s'.";

    /**
     * @return the parameter names that were bound to an extension, for unknown parameter detection
     */
    public static List<String> bind(final ConfigurationModel config,
        final String environmentName,
        final PluginRegister pluginRegister) {
        final EnvironmentModel environment = config.getEnvironments().get(environmentName);
        final Map<String, Object> effectivePluginConfigurations = MergeUtils.merge(config.getFlyway()
                .getPluginConfigurations(),
            environment == null ? Map.of() : environment.getFlyway().getPluginConfigurations(),
            MergeUtils::mergeObjects);

        final List<String> configuredPluginParameters = new ArrayList<>();
        for (final ConfigurationExtension configurationExtension : pluginRegister.getInstancesOf(
            ConfigurationExtension.class)) {
            bindNamespace(configurationExtension.getNamespace(),
                effectivePluginConfigurations,
                config.getRootConfigurations(),
                configurationExtension,
                configuredPluginParameters);
        }
        return configuredPluginParameters;
    }

    private static void bindNamespace(String namespace,
        Map<String, Object> pluginConfigs,
        final Map<String, Object> rootConfigurations,
        final ConfigurationExtension configurationExtension,
        final List<? super String> configuredPluginParameters) {
        boolean suppressError = false;

        if (namespace.startsWith("\\")) {
            suppressError = true;
            namespace = namespace.substring(1);
            pluginConfigs = rootConfigurations;
        }
        if (pluginConfigs.containsKey(namespace) || namespace.isEmpty()) {
            final List<String> fields = Arrays.stream(configurationExtension.getClass().getDeclaredFields())
                .map(Field::getName)
                .toList();
            Map<String, Object> values = !namespace.isEmpty()
                ? (Map<String, Object>) pluginConfigs.get(namespace)
                : pluginConfigs;

            values = values.entrySet()
                .stream()
                .filter(p -> fields.stream().anyMatch(k -> k.equalsIgnoreCase(p.getKey())))
                .collect(Collectors.toMap(p -> fields.stream()
                    .filter(q -> q.equalsIgnoreCase(p.getKey()))
                    .findFirst()
                    .orElse(p.getKey()), Map.Entry::getValue));

            try {
                if (configurationExtension.isStub()
                    && new HashSet<>(configuredPluginParameters).containsAll(values.keySet())) {
                    return;
                }

                final Map<String, Object> finalValues = values;
                Arrays.stream(configurationExtension.getClass().getDeclaredFields())
                    .filter(f -> List.of(Collection.class, List.class, String[].class).contains(f.getType()))
                    .forEach(f -> {
                        final String fieldName = f.getName();
                        final Object fieldValue = finalValues.get(fieldName);
                        if (fieldValue instanceof final String fieldValueString) {
                            finalValues.put(fieldName,
                                StringUtils.hasText(fieldValueString) ? fieldValueString.split(",") : new String[0]);
                        }
                    });

                final ObjectMapper mapper = getObjectMapper(suppressError);

                final ConfigurationExtension newConfigurationExtension = mapper.convertValue(finalValues,
                    configurationExtension.getClass());

                // Redo the entire mapping without suppressError only to print out the warning message.
                if (suppressError) {
                    try {
                        final ConfigurationExtension dummyConfigurationExtension = getObjectMapper(false).convertValue(
                            finalValues,
                            configurationExtension.getClass());
                    } catch (final IllegalArgumentException e) {
                        final var fullFieldName = getFullFieldNameFromException(namespace, e);

                        LOG.warn(String.format(UNABLE_TO_PARSE_FIELD, fullFieldName));
                    }
                }
                MergeUtils.mergeModel(newConfigurationExtension, configurationExtension);

                if (!values.isEmpty()) {
                    for (final Map.Entry<String, Object> entry : values.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?> && namespace.isEmpty()) {
                            final Map<String, Object> temp = (Map<String, Object>) entry.getValue();
                            configuredPluginParameters.addAll(temp.keySet());
                        } else {
                            configuredPluginParameters.add(entry.getKey());
                        }
                    }
                }
            } catch (final Exception e) {
                if (getRootCause(e) instanceof final FlywayRedgateEditionRequiredException cause) {
                    throw cause;
                }

                final var fullFieldName = getFullFieldNameFromException(namespace, e);
                var message = String.format(UNABLE_TO_PARSE_FIELD, fullFieldName) + "\n" + e.getMessage();
                message += getFlywayExceptionMessage(e).map(text -> " " + text).orElse("");

                if (suppressError) {
                    LOG.warn(message);
                } else {
                    LOG.error(message);
                }
            }
        }
    }

    private static String getFullFieldNameFromException(final String namespace, final Exception e) {
        final var matcher = ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN.matcher(e.getMessage());
        final var fullFieldName = new StringBuilder();
        if (!namespace.isEmpty()) {
            fullFieldName.append(namespace);
        }

        while (matcher.find()) {
            if (!fullFieldName.isEmpty()) {
                fullFieldName.append(".");
            }
            fullFieldName.append(matcher.group(1));
        }
        return fullFieldName.toString();
    }

    private static ObjectMapper getObjectMapper(final boolean suppressError) {
        final ObjectMapper mapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();

        if (suppressError) {
            return mapper.rebuild().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
        }

        return mapper;
    }
}
