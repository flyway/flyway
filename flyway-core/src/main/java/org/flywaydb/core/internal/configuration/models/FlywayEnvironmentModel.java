/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.configuration.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.MergeUtils;
import org.flywaydb.core.internal.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@ExtensionMethod(MergeUtils.class)
public class FlywayEnvironmentModel {

    public static final String DEFAULT_REPORT_FILENAME = "report";

    @Setter(lombok.AccessLevel.NONE)
    private String reportFilename;
    private Boolean detectEncoding;
    private String encoding;
    private String placeholderPrefix;
    private String placeholderSuffix;
    private String placeholderSeparator;
    private String scriptPlaceholderPrefix;
    private String scriptPlaceholderSuffix;
    private String sqlMigrationPrefix;
    private Boolean executeInTransaction;
    private String repeatableSqlMigrationPrefix;
    private String sqlMigrationSeparator;
    private List<String> sqlMigrationSuffixes;
    private Boolean cleanDisabled;
    private Boolean cleanOnValidationError;
    private Boolean communityDBSupportEnabled;
    private List<String> locations;
    private String table;
    private String tablespace;
    private String target;
    private Boolean failOnMissingTarget;
    private Boolean placeholderReplacement;
    private List<String> ignoreMigrationPatterns;
    private Boolean validateMigrationNaming;
    private Boolean validateOnMigrate;
    private String baselineVersion;
    private String baselineDescription;
    private Boolean baselineOnMigrate;
    private Boolean outOfOrder;
    private Boolean skipExecutingMigrations;
    private List<String> callbacks;
    private Boolean skipDefaultCallbacks;
    private List<String> migrationResolvers;
    private Boolean skipDefaultResolvers;
    private Boolean mixed;
    private Boolean group;
    private String installedBy;
    private Boolean createSchemas;
    private List<String> errorOverrides;
    private String dryRunOutput;
    private Boolean stream;
    private Boolean batch;
    private Boolean outputQueryResults;
    private Integer lockRetryCount;
    private String kerberosConfigFile;
    private Boolean failOnMissingLocations;
    private List<String> loggers;
    private Map<String, String> placeholders;
    private String defaultSchema;
    private Map<String, PropertyResolver> propertyResolvers;
    private Boolean reportEnabled;

    @JsonAnySetter
    @Getter(onMethod = @__(@ClassUtils.DoNotMapForLogging))
    private Map<String,Object> pluginConfigurations = new HashMap<>();

    public FlywayEnvironmentModel merge(FlywayEnvironmentModel otherPojo) {
        FlywayEnvironmentModel result = new FlywayEnvironmentModel();
        result.reportFilename = reportFilename.merge(otherPojo.reportFilename);
        result.encoding = encoding.merge(otherPojo.encoding);
        result.detectEncoding = detectEncoding.merge(otherPojo.detectEncoding);
        result.placeholderPrefix = placeholderPrefix.merge(otherPojo.placeholderPrefix);
        result.placeholderSuffix = placeholderSuffix.merge(otherPojo.placeholderSuffix);
        result.placeholderSeparator = placeholderSeparator.merge(otherPojo.placeholderSeparator);
        result.scriptPlaceholderPrefix = scriptPlaceholderPrefix.merge(otherPojo.scriptPlaceholderPrefix);
        result.scriptPlaceholderSuffix = scriptPlaceholderSuffix.merge(otherPojo.scriptPlaceholderSuffix);
        result.sqlMigrationPrefix = sqlMigrationPrefix.merge(otherPojo.sqlMigrationPrefix);
        result.executeInTransaction = executeInTransaction.merge(otherPojo.executeInTransaction);
        result.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix.merge(otherPojo.repeatableSqlMigrationPrefix);
        result.sqlMigrationSeparator = sqlMigrationSeparator.merge(otherPojo.sqlMigrationSeparator);
        result.sqlMigrationSuffixes = sqlMigrationSuffixes.merge(otherPojo.sqlMigrationSuffixes);
        result.cleanDisabled = cleanDisabled.merge(otherPojo.cleanDisabled);
        result.cleanOnValidationError = cleanOnValidationError.merge(otherPojo.cleanOnValidationError);
        result.communityDBSupportEnabled = communityDBSupportEnabled.merge(otherPojo.communityDBSupportEnabled);
        result.locations = locations.merge(otherPojo.locations);
        result.table = table.merge(otherPojo.table);
        result.tablespace = tablespace.merge(otherPojo.tablespace);
        result.target = target.merge(otherPojo.target);
        result.failOnMissingTarget = failOnMissingTarget.merge(otherPojo.failOnMissingTarget);
        result.placeholderReplacement = placeholderReplacement.merge(otherPojo.placeholderReplacement);
        result.ignoreMigrationPatterns = ignoreMigrationPatterns.merge(otherPojo.ignoreMigrationPatterns);
        result.validateMigrationNaming = validateMigrationNaming.merge(otherPojo.validateMigrationNaming);
        result.validateOnMigrate = validateOnMigrate.merge(otherPojo.validateOnMigrate);
        result.baselineVersion = baselineVersion.merge(otherPojo.baselineVersion);
        result.baselineDescription = baselineDescription.merge(otherPojo.baselineDescription);
        result.baselineOnMigrate = baselineOnMigrate.merge(otherPojo.baselineOnMigrate);
        result.outOfOrder = outOfOrder.merge(otherPojo.outOfOrder);
        result.skipExecutingMigrations = skipExecutingMigrations.merge(otherPojo.skipExecutingMigrations);
        result.callbacks = callbacks.merge(otherPojo.callbacks);
        result.skipDefaultCallbacks = skipDefaultCallbacks.merge(otherPojo.skipDefaultCallbacks);
        result.migrationResolvers = migrationResolvers.merge(otherPojo.migrationResolvers);
        result.skipDefaultResolvers = skipDefaultResolvers.merge(otherPojo.skipDefaultResolvers);
        result.mixed = mixed.merge(otherPojo.mixed);
        result.group = group.merge(otherPojo.group);
        result.installedBy = installedBy.merge(otherPojo.installedBy);
        result.createSchemas = createSchemas.merge(otherPojo.createSchemas);
        result.errorOverrides = errorOverrides.merge(otherPojo.errorOverrides);
        result.dryRunOutput = dryRunOutput.merge(otherPojo.dryRunOutput);
        result.stream = stream.merge(otherPojo.stream);
        result.batch = batch.merge(otherPojo.batch);
        result.outputQueryResults = outputQueryResults.merge(otherPojo.outputQueryResults);
        result.lockRetryCount = lockRetryCount.merge(otherPojo.lockRetryCount);
        result.kerberosConfigFile = kerberosConfigFile.merge(otherPojo.kerberosConfigFile);
        result.failOnMissingLocations = failOnMissingLocations.merge(otherPojo.failOnMissingLocations);
        result.loggers = loggers.merge(otherPojo.loggers);
        result.defaultSchema = defaultSchema.merge(otherPojo.defaultSchema);
        result.placeholders = MergeUtils.merge(placeholders, otherPojo.placeholders, (a,b) -> b != null ? b : a);
        result.reportEnabled = reportEnabled.merge(otherPojo.reportEnabled);
        result.propertyResolvers = MergeUtils.merge(propertyResolvers, otherPojo.propertyResolvers, (a,b) -> b != null ? b : a); // TODO: more granular merge
        result.pluginConfigurations = MergeUtils.merge(pluginConfigurations, otherPojo.pluginConfigurations, (a,b) -> b != null ? b : a);
        return result;
    }

    public void setReportFilename(String reportFilename) {
        if (StringUtils.hasText(reportFilename)) {
            this.reportFilename = reportFilename;
        }
    }

    @Override
    public String toString() {
        return ClassUtils.getGettableFieldValues(this, "").toString();
    }
}
