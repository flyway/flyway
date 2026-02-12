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
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Reason.REMOVED;
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Reason.REPLACED;
import static org.flywaydb.core.internal.configuration.models.UnknownParameterModel.Reason.UNKNOWN;

import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.util.StringUtils;

public record UnknownParameterModel(String rawKey, Reason reason, Source source, List<String> possibleValues, String replacement) {
    private static final String CLEAN_ON_VALIDATION_ERROR = "flyway.cleanOnValidationError";

    private static final Map<String, String> MOVED_OR_REMOVED_PARAMS = Map.ofEntries(
        Map.entry(CLEAN_ON_VALIDATION_ERROR, "")
        );

    public enum Reason {
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
        final String rawKey = prefix + key;
        final String configKey = namespace + "." + key;

        if (MOVED_OR_REMOVED_PARAMS.containsKey(configKey)) {
            final String replacement = MOVED_OR_REMOVED_PARAMS.get(configKey);
            if (StringUtils.hasText(replacement)) {
                return new UnknownParameterModel(rawKey, REPLACED, null, null, replacement);
            }

            return new UnknownParameterModel(rawKey, REMOVED, null, null, replacement);
        } else {
            final List<String> possibleConfigurations = getPossibleFlywayConfigurations(key, model, prefix);
            if (!possibleConfigurations.isEmpty()) {
                return new UnknownParameterModel(rawKey, UNKNOWN, null, possibleConfigurations, null);
            }

            return new UnknownParameterModel(rawKey, UNKNOWN, null, null, null);
        }
    }

    @Override
    public String toString() {
        return switch (reason) {
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
}
