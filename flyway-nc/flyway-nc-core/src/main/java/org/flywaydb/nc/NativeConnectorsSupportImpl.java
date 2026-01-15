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
package org.flywaydb.nc;

import static org.flywaydb.core.internal.nc.NativeConnectorsModeUtils.isNativeConnectorsTurnedOff;
import static org.flywaydb.core.internal.nc.NativeConnectorsModeUtils.isNativeConnectorsTurnedOn;
import static org.flywaydb.nc.utils.NativeConnectorsUtils.resolveExperimentalDatabasePlugin;

import java.util.Optional;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.nc.NativeConnectorsSupport;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;

@CustomLog
public class NativeConnectorsSupportImpl implements NativeConnectorsSupport {
    @Override
    public boolean canUseNativeConnectors(final Configuration configuration, final String verb) {
        if (isNativeConnectorsTurnedOff()) {
            return false;
        }

        if (configuration.getUrl() == null) {
            return false;
        }

        new ConfigurationValidator().validate(configuration);

        final Optional<NativeConnectorsDatabase> database = resolveExperimentalDatabasePlugin(configuration);

        final boolean canUseNativeConnectors =  database.map(experimentalDatabase -> experimentalDatabase.supportedVerbs().contains(verb) &&
                (experimentalDatabase.isOnByDefault(configuration) || isNativeConnectorsTurnedOn()))
            .orElse(false);

        if (canUseNativeConnectors && useLegacyAsDryRunSet(configuration)) {
            if (database.get() instanceof NativeConnectorsNonJdbc) {
                throw new FlywayException("Dry run is not supported for " + database.get().getDatabaseType());
            }

            LOG.warn("Dry run is not supported in Native Connectors mode, falling back to legacy databases");
            return false;
        }

        return canUseNativeConnectors;
    }

    @Override
    public boolean canCreateDataSource(final Configuration configuration) {
        if (!isNativeConnectorsTurnedOff()) {
            if (configuration.getUrl() == null) {
                return true;
            }

            Optional<NativeConnectorsDatabase> database = resolveExperimentalDatabasePlugin(configuration);

            return database.map(experimentalDatabase -> experimentalDatabase.canCreateJdbcDataSource()
                    || !experimentalDatabase.isOnByDefault(configuration))
                .orElse(true);
        }

        return true;
    }

    private static boolean useLegacyAsDryRunSet(final Configuration config) {
        return config.getDryRunOutput() != null;
    }
}
