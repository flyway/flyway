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
package org.flywaydb.core.internal.configuration.models;

import static org.flywaydb.core.internal.configuration.ConfigUtils.getPossibleFlywayConfigurations;
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Kind.REMOVED;
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Kind.REPLACED;
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Kind.UNKNOWN;

import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.util.StringUtils;

public record UnknownParameterModel(String rawKey, Kind kind, Source source, List<String> possibleValues, String replacement, String reason) {
    private static final String CLEAN_ON_VALIDATION_ERROR = "flyway.cleanOnValidationError";
    private static final String CHECK_MINOR_TOLERANCE = "check.minorTolerance";
    private static final String CHECK_MAJOR_TOLERANCE = "check.majorTolerance";
    private static final String CHECK_MINOR_RULES = "check.minorRules";
    private static final String CHECK_MAJOR_RULES = "check.majorRules";
    private static final String PLUGINS_CLEAN = "plugins.clean";
    private static final String SQLSERVER_CLEAN = "flyway.sqlserver.clean";
    private static final String PLUGINS_VAULT = "plugins.vault";
    private static final String FLYWAY_VAULT = "flyway.vault";
    private static final String PLUGINS_DAPR = "plugins.dapr";
    private static final String FLYWAY_DAPR = "flyway.dapr";
    private static final String PLUGINS_GCSM = "plugins.gcsm";
    private static final String FLYWAY_GCSM = "flyway.gcsm";

    private static final String PLUGINS_NAMESPACE_REASON = "The 'flyway.plugins' namespace was deprecated in Flyway V11.";
    private static final String CHECK_RULES_REASON = "Code analysis rules are now configured in your SQLFluff configuration.";
    private static final String CLEAN_ON_VALIDATION_ERROR_REASON = "Removed in Flyway V12. Refer to the V12 update guide for alternatives.";

    private record ObsoleteEntry(String replacement, String reason) {}

    private static final Map<String, ObsoleteEntry> MOVED_OR_REMOVED_PARAMS = Map.ofEntries(
        Map.entry(PLUGINS_CLEAN, new ObsoleteEntry(SQLSERVER_CLEAN, PLUGINS_NAMESPACE_REASON)),
        Map.entry(PLUGINS_VAULT, new ObsoleteEntry(FLYWAY_VAULT, PLUGINS_NAMESPACE_REASON)),
        Map.entry(PLUGINS_DAPR, new ObsoleteEntry(FLYWAY_DAPR, PLUGINS_NAMESPACE_REASON)),
        Map.entry(PLUGINS_GCSM, new ObsoleteEntry(FLYWAY_GCSM, PLUGINS_NAMESPACE_REASON)),
        Map.entry(CLEAN_ON_VALIDATION_ERROR, new ObsoleteEntry("", CLEAN_ON_VALIDATION_ERROR_REASON)),
        Map.entry(CHECK_MAJOR_TOLERANCE, new ObsoleteEntry("", CHECK_RULES_REASON)),
        Map.entry(CHECK_MINOR_TOLERANCE, new ObsoleteEntry("", CHECK_RULES_REASON)),
        Map.entry(CHECK_MINOR_RULES, new ObsoleteEntry("", CHECK_RULES_REASON)),
        Map.entry(CHECK_MAJOR_RULES, new ObsoleteEntry("", CHECK_RULES_REASON))
        );

    public enum Kind {
        REPLACED,
        REMOVED,
        UNKNOWN
    }

    public enum Source {
        TOML,
        CLI,
        ENV
    }

    public static UnknownParameterModel resolveUnknownParameter(final FlywayEnvironmentModel model,
        final String namespace, String key, String prefix) {
        final String rawKey = getRawKey(namespace, key, prefix);
        final String configKey = namespace + "." + key;

        if (MOVED_OR_REMOVED_PARAMS.containsKey(configKey)) {
            final ObsoleteEntry entry = MOVED_OR_REMOVED_PARAMS.get(configKey);
            if (StringUtils.hasText(entry.replacement())) {
                return new UnknownParameterModel(rawKey, REPLACED, null, null, entry.replacement(), entry.reason());
            }

            return new UnknownParameterModel(rawKey, REMOVED, null, null, null, entry.reason());
        } else {
            final List<String> possibleConfigurations = getPossibleFlywayConfigurations(key, model, prefix);
            if (!possibleConfigurations.isEmpty()) {
                return new UnknownParameterModel(rawKey, UNKNOWN, null, possibleConfigurations, null, null);
            }

            return new UnknownParameterModel(rawKey, UNKNOWN, null, null, null, null);
        }
    }

    @Override
    public String toString() {
        return switch (kind) {
            case REPLACED -> "\tDeprecated: '" + rawKey + "' has been replaced by '" + replacement + "'.";
            case REMOVED -> "\tRemoved: '" + rawKey + "' has been removed and is no longer supported.";
            case UNKNOWN -> {
                if (possibleValues != null && !possibleValues.isEmpty()) {
                    yield "\tUnknown: '" + rawKey + "'. Possible values: "
                        + String.join(", ", possibleValues) + "?";
                }
                yield "\tUnknown: '" + rawKey + "'." + " Check the parameter spelling and ensure Flyway is up to date.";
            }
        };
    }

    private static String getRawKey(final String namespace, final String key, final String prefix) {
        if (prefix.endsWith(namespace + ".")) {
            return prefix + key;
        } else {
            return prefix + namespace + "." + key;
        }
    }
}
