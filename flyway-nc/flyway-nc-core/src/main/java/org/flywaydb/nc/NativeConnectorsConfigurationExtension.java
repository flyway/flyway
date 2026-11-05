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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.ConfigurationExtension;

public class NativeConnectorsConfigurationExtension implements ConfigurationExtension {
    static final int DEFAULT_PROCESS_TIMEOUT = 300;

    private Integer processTimeout;

    /**
     * @return The maximum migration process execution time in seconds. Defaults to 300 seconds.
     */
    public int getProcessTimeout() {
        return processTimeout == null ? DEFAULT_PROCESS_TIMEOUT : processTimeout;
    }

    /**
     * @param processTimeout The maximum migration process execution time in seconds. Must be positive.
     */
    public void setProcessTimeout(final int processTimeout) {
        if (processTimeout <= 0) {
            throw new FlywayException("flyway.nativeConnectors.processTimeout must be greater than 0");
        }
        this.processTimeout = processTimeout;
    }

    @Override
    public String getNamespace() {
        return "nativeConnectors";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(final String environmentVariable) {
        if ("FLYWAY_NATIVE_CONNECTORS_PROCESS_TIMEOUT".equals(environmentVariable)) {
            return "flyway.nativeConnectors.processTimeout";
        }
        return null;
    }
}
