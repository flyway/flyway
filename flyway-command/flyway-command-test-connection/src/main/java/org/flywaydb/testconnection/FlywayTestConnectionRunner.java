/*-
 * ========================LICENSE_START=================================
 * flyway-command-test-connection
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
package org.flywaydb.testconnection;

import static org.flywaydb.core.internal.nc.NativeConnectorsModeUtils.canUseNativeConnectors;

import lombok.CustomLog;
import org.flywaydb.core.FlywayExecutor;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.TestConnectionRunner;
import org.flywaydb.nc.utils.VerbUtils;

@CustomLog
public class FlywayTestConnectionRunner implements TestConnectionRunner {

    private static final String CONNECTION_SUCCESSFUL = "Flyway engine connection successful";

    @Override
    public String testConnection(final Configuration configuration) {
        final ProgressLogger progress = configuration.createProgress("testConnection");

        // Allows provision progress to be tracked - this call could be removed if progress were supported in core
        configuration.resolveCurrentEnvironment(progress);

        if (canUseNativeConnectors(configuration)) {
            // Open-and-close to validate the connection
            try (final var ignored = VerbUtils.getNativeConnectorsDatabase(configuration)) {
                LOG.info(CONNECTION_SUCCESSFUL);
                return "Flyway";
            } catch (final Exception e) {
                throw new FlywayException("Connection failed", e);
            }
        }

        try (final var ignored = new FlywayExecutor(configuration).init()) {
            LOG.info(CONNECTION_SUCCESSFUL);
            return "Flyway";
        }
    }
}
