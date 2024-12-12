/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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

import java.util.List;
import java.util.Map;
import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;

@CustomLog
public class ExperimentalModeUtils {

    private static final Map<String, List<String>> EXPERIMENTAL_DATABASES = Map.of(




        "SQLite",
        List.of("info", "validate", "migrate", "clean", "undo", "baseline", "repair"));

    private static final Map<String, List<String>> DEFAULT_DATABASES = Map.of("mongodb",
        List.of("info", "validate", "migrate", "clean", "baseline", "repair", "undo"));

    private static boolean isExperimentalModeActivated(final Configuration configuration) {
        if ("OSS".equals(LicenseGuard.getTierAsString(configuration))) {
            return System.getenv("FLYWAY_NATIVE_CONNECTORS") == null || System.getenv("FLYWAY_NATIVE_CONNECTORS").equalsIgnoreCase("true");
        }
        return System.getenv("FLYWAY_NATIVE_CONNECTORS") != null && System.getenv("FLYWAY_NATIVE_CONNECTORS").equalsIgnoreCase("true");
    }

    public static boolean canUseExperimentalMode(final Configuration config,  final String verb) {
        if (!isExperimentalModeActivated(config)) {
            return false;
        }

        if (useLegacyAsDryRunSet(config)) {
            LOG.warn("Dry run is not supported in experimental databases, falling back to legacy databases");
            return false;
        }

        new ConfigurationValidator().validate(config);

        String database = getCurrentDatabase(config);

        if (database == null) {
            return false;
        }

        if (DEFAULT_DATABASES.containsKey(database)) {
            return DEFAULT_DATABASES.get(database).contains(verb);
        }

        if (EXPERIMENTAL_DATABASES.containsKey(database)
        && System.getenv("FLYWAY_NATIVE_CONNECTORS") != null) {
            return EXPERIMENTAL_DATABASES.get(database).contains(verb);
        }

        return false;
    }

    public static boolean canCreateDataSource(final Configuration config) {
        if (useLegacyAsDryRunSet(config)) {
            return true;
        }

        final String database = getCurrentDatabase(config);
        if (isExperimentalModeActivated(config)) {
            return !"mongodb".equals(database);
        }

        return true;
    }

    public static void logExperimentalDataTelemetry(final FlywayTelemetryManager flywayTelemetryManager, final MetaData metaData) {
        if (flywayTelemetryManager != null) {
            RootTelemetryModel rootTelemetryModel = flywayTelemetryManager.getRootTelemetryModel();
            if (rootTelemetryModel != null) {
                rootTelemetryModel.setDatabaseEngine(metaData.databaseProductName());
                rootTelemetryModel.setDatabaseVersion(metaData.databaseProductVersion());
                rootTelemetryModel.setConnectionType(metaData.connectionType().name());
                rootTelemetryModel.setExperimentalMode(true);
            }
        }
    }

    private static boolean useLegacyAsDryRunSet(final Configuration config) {
        return config.getDryRunOutput() != null;
    }

    private static String getCurrentDatabase(final Configuration config) {
        if (config.getUrl() == null) {
            return null;
        }

        if (config.getUrl().startsWith("mongodb") || config.getUrl().startsWith("jdbc:mongodb")) {
            return "mongodb";
        }







        if (config.getUrl().startsWith("jdbc:sqlite")) {
            return "SQLite";
        }

        return null;
    }
}
