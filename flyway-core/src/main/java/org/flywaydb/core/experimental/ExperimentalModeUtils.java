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

import java.util.Optional;
import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;

@CustomLog
public class ExperimentalModeUtils {

    public static boolean canUseExperimentalMode(final Configuration config,  final String verb) {
        if (isNativeConnectorsTurnedOff()) {
            return false;
        }

        if (useLegacyAsDryRunSet(config)) {
            LOG.warn("Dry run is not supported in Native Connectors mode, falling back to legacy databases");
            return false;
        }

        if (config.getUrl() == null) {
            return false;
        }

        new ConfigurationValidator().validate(config);

        Optional<ExperimentalDatabase> database = resolveExperimentalDatabasePlugin(config);

        return database.map(experimentalDatabase -> experimentalDatabase.supportedVerbs().contains(verb) &&
                (experimentalDatabase.isOnByDefault(config) || isNativeConnectorsTurnedOn()))
            .orElse(false);
    }

    public static boolean canCreateDataSource(final Configuration config) {
        if (useLegacyAsDryRunSet(config)) {
            return true;
        }

        if (!isNativeConnectorsTurnedOff()) {
            if (config.getUrl() == null) {
                return true;
            }

            Optional<ExperimentalDatabase> database = resolveExperimentalDatabasePlugin(config);

            return database.map(experimentalDatabase -> experimentalDatabase.canCreateJdbcDataSource()
                               || !experimentalDatabase.isOnByDefault(config))
                .orElse(true);
        }

        return true;
    }

    public static void logExperimentalDataTelemetry(final FlywayTelemetryManager flywayTelemetryManager, final MetaData metaData) {
        if (flywayTelemetryManager != null) {
            flywayTelemetryManager.notifyExperimentalMetadataChanged(metaData);
        }
    }

    private static boolean useLegacyAsDryRunSet(final Configuration config) {
        return config.getDryRunOutput() != null;
    }

    public static Optional<ExperimentalDatabase> resolveExperimentalDatabasePlugin(final Configuration configuration) {
        return new ExperimentalDatabasePluginResolverImpl(configuration.getPluginRegister())
            .resolve(configuration.getUrl());
    }

    private static boolean isNativeConnectorsTurnedOn() {
        return System.getenv("FLYWAY_NATIVE_CONNECTORS") != null && System.getenv("FLYWAY_NATIVE_CONNECTORS").equalsIgnoreCase("true");
    }

    private static boolean isNativeConnectorsTurnedOff() {
        return System.getenv("FLYWAY_NATIVE_CONNECTORS") != null && System.getenv("FLYWAY_NATIVE_CONNECTORS").equalsIgnoreCase("false");
    }
}
