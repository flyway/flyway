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
package org.flywaydb.core.internal.logging;

import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;

@CustomLog
public class PreviewFeatureWarning {
    private static final List<String> LOGGED_FEATURES = new ArrayList<>();
    
    public static final String NATIVE_CONNECTORS = "Native Connectors";
    public static boolean isPreviewFeatureEnabled(String featureName, String environmentVariable, boolean showHowToEnable) {
        if (System.getenv(environmentVariable) != null) {
            logPreviewFeature(featureName);
            return true;
        } else {
            if (showHowToEnable) {
                LOG.debug("Preview feature '" + featureName + "' is disabled.");
                LOG.debug("Enable it by setting the environment variable " + environmentVariable + "=true");
            }
        }
        return false;
    }
    public static void logPreviewFeature(String featureName) {
        if (LOGGED_FEATURES.contains(featureName)) {
            return;
        }

        LOG.info("-----------------------------------------------------------------------------");
        LOG.info("You are using a preview feature '" + featureName + "'.");
        LOG.info("Please report any issues you encounter to DatabaseDevOps@red-gate.com");
        LOG.info("-----------------------------------------------------------------------------");

        LOGGED_FEATURES.add(featureName);
    }
}
