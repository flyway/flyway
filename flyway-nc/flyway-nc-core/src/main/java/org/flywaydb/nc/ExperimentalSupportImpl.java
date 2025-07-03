/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc;

import static org.flywaydb.core.experimental.ExperimentalModeUtils.isNativeConnectorsTurnedOff;
import static org.flywaydb.core.experimental.ExperimentalModeUtils.isNativeConnectorsTurnedOn;
import static org.flywaydb.nc.utils.NativeConnectorsUtils.resolveExperimentalDatabasePlugin;

import java.util.Optional;
import lombok.CustomLog;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.ExperimentalSupport;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;

@CustomLog
public class ExperimentalSupportImpl implements ExperimentalSupport {
    @Override
    public boolean canUseNativeConnectors(final Configuration configuration, final String verb) {
        if (isNativeConnectorsTurnedOff()) {
            return false;
        }

        if (useLegacyAsDryRunSet(configuration)) {

            if (isNativeConnectorsTurnedOn()) {
                LOG.warn("Dry run is not supported in Native Connectors mode, falling back to legacy databases");
            }

            return false;
        }

        if (configuration.getUrl() == null) {
            return false;
        }

        new ConfigurationValidator().validate(configuration);

        final Optional<ExperimentalDatabase> database = resolveExperimentalDatabasePlugin(configuration);

        return database.map(experimentalDatabase -> experimentalDatabase.supportedVerbs().contains(verb) &&
                (experimentalDatabase.isOnByDefault(configuration) || isNativeConnectorsTurnedOn()))
            .orElse(false);
    }

    @Override
    public boolean canCreateDataSource(final Configuration configuration) {
        if (useLegacyAsDryRunSet(configuration)) {
            return true;
        }

        if (!isNativeConnectorsTurnedOff()) {
            if (configuration.getUrl() == null) {
                return true;
            }

            Optional<ExperimentalDatabase> database = resolveExperimentalDatabasePlugin(configuration);

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
