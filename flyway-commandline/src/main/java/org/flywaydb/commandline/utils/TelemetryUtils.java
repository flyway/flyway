/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.InfoOutput;
import org.flywaydb.core.extensibility.RgDomainChecker;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelemetryUtils {

    public static RootTelemetryModel populateRootTelemetry(RootTelemetryModel rootTelemetryModel, Configuration configuration, boolean isRedgateEmployee) {
        rootTelemetryModel.setRedgateEmployee(isRedgateEmployee);

        if (configuration != null) {
            ConfigurationModel modernConfig = configuration.getModernConfig();
            if (modernConfig != null && StringUtils.hasText(modernConfig.getId())) {
                rootTelemetryModel.setProjectId(hashProjectId(modernConfig.getId()));
            }
        }

        return rootTelemetryModel;
    }

    static String hashProjectId(String projectId) {
        if (projectId == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(projectId.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest("fur".getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            String result = number.toString(16);
            while (result.length() < 64) {
                result = "0" + result;
            }
            return result;
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    public static boolean isRedgateEmployee(PluginRegister pluginRegister, Configuration configuration) {
        RgDomainChecker domainChecker = pluginRegister.getPlugin(RgDomainChecker.class);
        if (domainChecker == null) {
            return false;
        }
        return domainChecker.isInDomain(configuration);
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