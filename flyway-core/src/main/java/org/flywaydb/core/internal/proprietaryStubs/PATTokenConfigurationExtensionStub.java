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
package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;

@CustomLog
public class PATTokenConfigurationExtensionStub implements ConfigurationExtension {

    private static final String FLYWAY_EMAIL = "flyway.email";
    private static final String FLYWAY_TOKEN = "flyway.token";
    private String email; //Do not delete this field. Config discovers valid parameters by looking at declared fields on config extensions.
    private String token; //Do not delete this field. Config discovers valid parameters by looking at declared fields on config extensions.

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(final String environmentVariable) {
        if ("FLYWAY_EMAIL".equals(environmentVariable)) {
            return FLYWAY_EMAIL;
        }
        if ("FLYWAY_TOKEN".equals(environmentVariable)) {
            return FLYWAY_TOKEN;
        }
        return null;
    }

    public void setEmail(String email) {
        throw new FlywayRedgateEditionRequiredException("PAT Token");
    }

    public void setToken(String token) {
        throw new FlywayRedgateEditionRequiredException("PAT Token");
    }

    public String getEmail() {
        return null;
    }

    public String getToken() {
        return null;
    }

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public Plugin copy() {
        return this;
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
