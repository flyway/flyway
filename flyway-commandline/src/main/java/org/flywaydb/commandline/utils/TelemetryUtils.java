/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.commandline.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.InfoOutput;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.license.EncryptionUtils;
import org.flywaydb.core.internal.license.FlywayPermit;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod(Tier.class)
public class TelemetryUtils {

    public static RootTelemetryModel populateRootTelemetry(RootTelemetryModel rootTelemetryModel, Configuration configuration, FlywayPermit flywayPermit) {

        rootTelemetryModel.setApplicationVersion(VersionPrinter.getVersion());

        boolean isRGDomainSet = System.getenv("RGDOMAIN") != null;

        if (flywayPermit != null) {
            rootTelemetryModel.setRedgateEmployee(flywayPermit.isRedgateEmployee() || isRGDomainSet);
            rootTelemetryModel.setApplicationEdition(flywayPermit.getTier().asString());
            rootTelemetryModel.setTrial(flywayPermit.isTrial());
            rootTelemetryModel.setSignedIn(flywayPermit.isFromAuth());
        } else {
            rootTelemetryModel.setRedgateEmployee(isRGDomainSet);
        }

        if (configuration != null) {
            ConfigurationModel modernConfig = configuration.getModernConfig();
            if (modernConfig != null && StringUtils.hasText(modernConfig.getId())) {
                rootTelemetryModel.setProjectId(EncryptionUtils.hashString(modernConfig.getId(), "fur"));
            }
        }

        return rootTelemetryModel;
    }

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
}