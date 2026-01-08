/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.utils;

import java.util.Optional;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.nc.NativeConnectorsDatabasePluginResolverImpl;
import org.flywaydb.core.internal.nc.MetaData;

public class NativeConnectorsUtils {
    public static void logExperimentalDataTelemetry(final FlywayTelemetryManager flywayTelemetryManager, final MetaData metaData) {
        if (flywayTelemetryManager != null) {
            flywayTelemetryManager.notifyDatabaseChanged(metaData.databaseType(),
                metaData.version().toString(),
                null);

            flywayTelemetryManager.notifyExperimentalModeChanged(true);
        }
    }

    public static Optional<NativeConnectorsDatabase> resolveExperimentalDatabasePlugin(final Configuration configuration) {
        return new NativeConnectorsDatabasePluginResolverImpl(configuration.getPluginRegister())
            .resolve(configuration);
    }
}
