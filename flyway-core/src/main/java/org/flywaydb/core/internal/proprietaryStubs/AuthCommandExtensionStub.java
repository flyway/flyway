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
package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.util.List;

@CustomLog
public class AuthCommandExtensionStub implements CommandExtension {
    private static final String FEATURE_NAME = "Auth";
    public static final String COMMAND = FEATURE_NAME.toLowerCase();
    public static final String DESCRIPTION = "Authenticates Flyway with Redgate licensing";

    @Override
    public boolean handlesCommand(String command) {
        return COMMAND.equals(command);
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @Override
    public OperationResult handle(String command, Configuration config, List<String> flags, FlywayTelemetryManager flywayTelemetryManager) throws FlywayException {
        throw new FlywayRedgateEditionRequiredException(FEATURE_NAME);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int getPriority() {
        return -100;
    }
}
