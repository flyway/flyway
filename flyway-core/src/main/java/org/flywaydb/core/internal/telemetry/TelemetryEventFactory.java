/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.telemetry;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.RgDomainChecker;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FingerprintUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelemetryEventFactory {
    public static Map<String, String> createUpdateCheckRequest(Configuration config, List<String> verbs, String dbEngine, String dbVersion) throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("currentVersion", VersionPrinter.getVersion());
        properties.put("operatingSystem", System.getProperty("os.name"));
        properties.put("fingerprint", FingerprintUtils.getFingerprint(config));
        properties.put("timeStamp", Instant.now().toString());
        properties.put("edition", VersionPrinter.EDITION.name());
        properties.put("verbs", Arrays.toString(verbs.toArray()));
        properties.put("engine", dbEngine);
        properties.put("version", dbVersion);
        properties.put("fromRedgate", Boolean.toString(isInRedgate(config)));
        return properties;
    }

    private static boolean isInRedgate(Configuration config) {
        if (System.getenv("RGDOMAIN") != null) {
            return true;
        }
        return config.getPluginRegister()
                     .getPlugins(RgDomainChecker.class)
                     .stream()
                     .anyMatch(c -> c.isInDomain(config));
    }
}