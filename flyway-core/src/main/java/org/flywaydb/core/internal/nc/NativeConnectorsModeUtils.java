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
package org.flywaydb.core.internal.nc;

import lombok.CustomLog;
import org.flywaydb.core.api.configuration.Configuration;

@CustomLog
public class NativeConnectorsModeUtils {

    public static boolean canUseNativeConnectors(final Configuration config,  final String verb) {
        final NativeConnectorsSupport supportChecker = config.getPluginRegister().getInstanceOf(
            NativeConnectorsSupport.class);
        if (supportChecker == null) {
            return false;
        }
        return supportChecker.canUseNativeConnectors(config, verb);
    }

    public static boolean canCreateDataSource(final Configuration config) {
        final NativeConnectorsSupport supportChecker = config.getPluginRegister().getInstanceOf(
            NativeConnectorsSupport.class);
        if (supportChecker == null) {
            return true;
        }
        return supportChecker.canCreateDataSource(config);
    }

    public static boolean isNativeConnectorsTurnedOn() {
        return System.getenv("FLYWAY_NATIVE_CONNECTORS") != null && System.getenv("FLYWAY_NATIVE_CONNECTORS").equalsIgnoreCase("true");
    }

    public static boolean isNativeConnectorsTurnedOff() {
        return System.getenv("FLYWAY_NATIVE_CONNECTORS") != null && System.getenv("FLYWAY_NATIVE_CONNECTORS").equalsIgnoreCase("false");
    }
}
