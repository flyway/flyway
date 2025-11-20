/*-
 * ========================LICENSE_START=================================
 * flyway-command-test-connection
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
package org.flywaydb.testconnection;

import static org.flywaydb.core.internal.nc.NativeConnectorsModeUtils.canUseNativeConnectors;
import static org.flywaydb.testconnection.TestConnectionCommandExtension.VERB;

import lombok.CustomLog;
import org.flywaydb.core.FlywayExecutor;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.TestConnectionRunner;
import org.flywaydb.core.extensibility.VerbExtension;

@CustomLog
public class FlywayTestConnectionRunner implements TestConnectionRunner {

    private static final String CONNECTION_SUCCESSFUL = "Flyway engine connection successful";

    @Override
    public String testConnection(final Configuration configuration) {

        if (canUseNativeConnectors(configuration, VERB)) {
            final var verb = configuration.getPluginRegister()
                .getInstancesOf(VerbExtension.class)
                .stream()
                .filter(verbExtension -> verbExtension.handlesVerb(VERB))
                .findFirst();
            if (verb.isPresent()) {
                LOG.debug("Native Connectors for testConnection is set and a verb is present");

                verb.get().executeVerb(configuration);

                LOG.info(CONNECTION_SUCCESSFUL);
                return "Flyway";
            } else {
                LOG.warn("Native Connectors for testConnection is set but no verb is present");
            }
        }

        try (final var ignored = new FlywayExecutor(configuration).init()) {
            LOG.info(CONNECTION_SUCCESSFUL);
            return "Flyway";
        }
    }
}
