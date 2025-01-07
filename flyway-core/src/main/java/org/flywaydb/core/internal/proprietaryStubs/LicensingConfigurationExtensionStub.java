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
package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.Plugin;

import static org.flywaydb.core.internal.util.FlywayDbWebsiteLinks.REDGATE_EDITION_DOWNLOAD;

@CustomLog
public class LicensingConfigurationExtensionStub implements ConfigurationExtension {

    private static final String LICENSE_KEY = "flyway.licenseKey";

    private String licenseKey; //This is actually needed. Config discovers valid parameters by looking at declared fields on config extensions.

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_LICENSE_KEY".equals(environmentVariable)) {
            return LICENSE_KEY;
        }
        return null;
    }

    public void setLicenseKey(String licenseKey) {
        LOG.warn("Attempting to set a license key in Flyway open-source. Redgate features will not be available. Download Redgate Flyway at " + REDGATE_EDITION_DOWNLOAD);
    }

    public String getLicenseKey() {
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
