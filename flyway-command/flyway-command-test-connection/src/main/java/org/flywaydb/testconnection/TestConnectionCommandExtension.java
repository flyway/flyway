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

import static org.flywaydb.core.internal.util.TelemetryUtils.getTelemetryManager;

import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.FlywayExecutor;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.TelemetrySpan;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.util.Pair;

@CustomLog
public class TestConnectionCommandExtension implements CommandExtension {
    static final String VERB = "testConnection";

    @Override
    public boolean handlesCommand(final String command) {
        return VERB.equals(command);
    }

    @Override
    public boolean handlesParameter(final String parameter) {
        return false;
    }

    @Override
    public List<Pair<String, String>> getUsage() {
        return List.of(Pair.of(VERB,
            "Attempts to establish a connection to the database using the configured connection settings"));
    }

    @Override
    public String getDescription() {
        return """
               Attempts to establish a connection to the database using the configured connection settings.
               If successful, Flyway exits with a zero status code.
               If unsuccessful, Flyway exits with a non-zero status code and an error message is displayed.
               """;
    }

    @Override
    public String getExample() {
        return "flyway " + VERB + " -environment=development";
    }

    @Override
    public OperationResult handle(final String command, final Configuration config, final List<String> flags)
        throws FlywayException {

        final FlywayTelemetryManager flywayTelemetryManager = getTelemetryManager(config);

        return TelemetrySpan.trackSpan(new EventTelemetryModel(VERB, flywayTelemetryManager), telemetryModel -> {
            try (final var jdbcConnectionFactory = new FlywayExecutor(config).init()) {
                LOG.info("Connection successful");
                return new TestConnectionResult(jdbcConnectionFactory.getDatabaseType().getName());
            }
        });
    }
}
