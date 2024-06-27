/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
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
package org.flywaydb.commandline.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.InfoOutput;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.license.EncryptionUtils;
import org.flywaydb.core.internal.license.FlywayPermit;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;







@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod(Tier.class)
public class TelemetryUtils {

    private static final String NON_REDGATE_DOCKER = "Non-Redgate Docker";
    private static final String NON_REDGATE_CONTAINER = "Non-Redgate Container";
    private static final String UNKNOWN = "Unknown";
    private static final String NOT_CONTAINER = "Not container";
    private static final String REDGATE_DOCKER = "Redgate Docker";

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

            rootTelemetryModel.setSecretsManagementType(getSecretsManagementType(configuration));
        }
        
        rootTelemetryModel.setContainerType(getContainerType(Paths::get));

        return rootTelemetryModel;
    }
    
    public static String getContainerType(java.util.function.Function<String, Path> getPath){
        final var redgateDocker = System.getenv("REDGATE_DOCKER");
        if("true".equals(redgateDocker)) {
            return REDGATE_DOCKER;
        }
        
        final var osName = System.getProperty("os.name", "generic");
        if(osName.startsWith("Windows")) {
            return NOT_CONTAINER;
        }

        // https://www.baeldung.com/linux/is-process-running-inside-container
        final var cgroupPath = getPath.apply("/proc/1/cgroup");
        if(Files.exists(cgroupPath)) {
            try (final var lines = Files.lines(cgroupPath)) {
                final var groups = lines.map(x -> x.split(":")[2]).toList();
                if (groups.stream().anyMatch(line -> line.startsWith("/docker"))) {
                    return NON_REDGATE_DOCKER;
                }
                if (groups.stream().anyMatch(line -> line.startsWith("/lxc"))) {
                    return NON_REDGATE_CONTAINER;
                }
            } catch (IOException e) {
                return UNKNOWN;
            }
        }

        final var schedPath = getPath.apply("/proc/1/sched");
        if(Files.exists(schedPath)) {
            try (final var lines = Files.lines(schedPath)){
                final var firstLine = lines.findFirst();
                if (firstLine.isPresent()) {
                    if (firstLine.get().contains("init") || firstLine.get().contains("system")) {
                        return NOT_CONTAINER;
                    }
                }
            } catch (IOException e) {
                return UNKNOWN;
            }
        }

        return UNKNOWN;
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

    private static String getSecretsManagementType(Configuration configuration) {
















        return "None";
    }
}
