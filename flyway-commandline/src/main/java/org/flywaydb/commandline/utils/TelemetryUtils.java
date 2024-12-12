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

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.InfoOutput;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.FlywayModel;
import org.flywaydb.core.internal.license.EncryptionUtils;
import org.flywaydb.core.internal.license.FlywayPermit;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.DockerUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;







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
            if (modernConfig != null) {
                if (StringUtils.hasText(modernConfig.getId())) {
                    rootTelemetryModel.setProjectId(EncryptionUtils.hashString(modernConfig.getId(), "fur"));
                }
            }

            boolean resolversPresent = configuration.getResolvers().length != 0;
            rootTelemetryModel.setCustomMigrationResolver(resolversPresent);

            rootTelemetryModel.setSecretsManagementType(getSecretsManagementType(configuration));

            final Map<String, Boolean> customParameters = new HashMap<>();
            FlywayModel defaults = FlywayModel.defaults();
            customParameters.put("validateOnMigrate", configuration.isValidateOnMigrate() != defaults.getValidateOnMigrate());
            customParameters.put("validateMigrationNaming", configuration.isValidateMigrationNaming() != defaults.getValidateMigrationNaming());
            customParameters.put("target", !Objects.equals(configuration.getTarget().getName(),
                defaults.getTarget()));
            customParameters.put("stream", configuration.isStream() != defaults.getStream());
            customParameters.put("reportEnabled", configuration.isReportEnabled() != defaults.getReportEnabled());
            customParameters.put("lockRetryCount", configuration.getLockRetryCount() != defaults.getLockRetryCount());
            customParameters.put("failOnMissingLocations", configuration.isFailOnMissingLocations() != defaults.getFailOnMissingLocations());
            customParameters.put("outputQueryResults", configuration.isOutputQueryResults() != defaults.getOutputQueryResults());
            customParameters.put("batch", configuration.isBatch() != defaults.getBatch());
            customParameters.put("createSchemas", configuration.isCreateSchemas() != defaults.getCreateSchemas());
            customParameters.put("baselineOnMigrate", configuration.isBaselineOnMigrate() != defaults.getBaselineOnMigrate());
            customParameters.put("group", configuration.isGroup() != defaults.getGroup());
            customParameters.put("mixed", configuration.isMixed() != defaults.getMixed());
            customParameters.put("outOfOrder", configuration.isOutOfOrder() != defaults.getOutOfOrder());
            customParameters.put("communityDBSupportEnabled", configuration.isCommunityDBSupportEnabled() != defaults.getCommunityDBSupportEnabled());
            customParameters.put("skipDefaultResolvers", configuration.isSkipDefaultResolvers() != defaults.getSkipDefaultResolvers());
            customParameters.put("skipDefaultCallbacks", configuration.isSkipDefaultCallbacks() != defaults.getSkipDefaultCallbacks());
            customParameters.put("skipExecutingMigrations", configuration.isSkipExecutingMigrations() != defaults.getSkipExecutingMigrations());
            customParameters.put("executeInTransaction", configuration.isExecuteInTransaction() != defaults.getExecuteInTransaction());
            customParameters.put("encoding", !Objects.equals(configuration.getEncoding().name(), defaults.getEncoding()));
            customParameters.put("detectEncoding", configuration.isDetectEncoding() != defaults.getDetectEncoding());
            customParameters.put("table", !Objects.equals(configuration.getTable(), defaults.getTable()));

            List<String> parameterNames = new ArrayList<>();
            customParameters.forEach((paramName, isSet) -> {
                if (isSet) {
                    parameterNames.add(paramName);
                }
            });
            rootTelemetryModel.setCustomParameters(String.join(",", parameterNames));

        }
        
        rootTelemetryModel.setContainerType(DockerUtils.getContainerType(Paths::get));

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

    private static String getSecretsManagementType(Configuration configuration) {

















        return "None";
    }
}
