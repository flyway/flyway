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
package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.InfoOutput;
import org.flywaydb.core.extensibility.Tier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod(Tier.class)
public class TelemetryUtils {
    /**
     * @param infos a List of InfoOutput
     *
     * @return the oldest migration date as UTC String
     * If no applied migration found, returns an empty string.
     */
    public static String getOldestMigration(List<InfoOutput> infos) {

        if (infos == null) {
            return "";
        }

        List<String> migrationDates = new ArrayList<>();

        infos.stream().filter(output -> StringUtils.hasText(output.installedOnUTC))
             .forEach(output -> migrationDates.add(output.installedOnUTC));

        if (!migrationDates.isEmpty()) {
            migrationDates.sort(Comparator.naturalOrder());
            return migrationDates.get(0);
        } else {
            return "";
        }
    }

    public static FlywayTelemetryManager getTelemetryManager(final Configuration configuration) {
        return configuration != null ? configuration.getPluginRegister().getInstanceOf(FlywayTelemetryManager.class) : null;
    }
}
