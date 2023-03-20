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
package org.flywaydb.core.internal.configuration.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.internal.util.MergeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ExtensionMethod(MergeUtils.class)
public class FlywayModel {
    private String environment;
    private Boolean detectEncoding;
    private String encoding;
    private String placeholderPrefix;
    private String placeholderSuffix;
    private String placeholderSeparator;
    private String scriptPlaceholderPrefix;
    private String scriptPlaceholderSuffix;
    private String sqlMigrationPrefix;
    private String undoSqlMigrationPrefix;
    private Boolean executeInTransaction;
    private String repeatableSqlMigrationPrefix;
    private String sqlMigrationSeparator;
    private List<String> sqlMigrationSuffixes;
    private String licenseKey;
    private Boolean oracleSqlplus;
    private Boolean oracleSqlplusWarn;
    private Boolean cleanDisabled;
    private Boolean cleanOnValidationError;
    private List<String> locations;
    private String table;
    private String tablespace;
    private String target;
    private Boolean failOnMissingTarget;
    private List<String> cherryPick;
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
    private String oracleKerberosCacheFile;
    private String oracleWalletLocation;
    private Boolean failOnMissingLocations;
    private List<String> loggers;
    private Map<String, String> placeholders;
    private String defaultSchema;
    private Map<String, PropertyResolver> propertyResolvers;

    public static FlywayModel defaults(){
         FlywayModel model = new FlywayModel();
         model.detectEncoding = false;
         model.encoding = "UTF-8";
         model.executeInTransaction = true;
         model.placeholderPrefix = "${";
         model.placeholderSuffix = "}";
         model.placeholderSeparator = ":";
         model.scriptPlaceholderPrefix = "FP__";
         model.scriptPlaceholderSuffix = "__";
         model.sqlMigrationPrefix = "V";
         model.undoSqlMigrationPrefix = "U";
         model.repeatableSqlMigrationPrefix = "R";
         model.sqlMigrationSeparator = "__";
         model.sqlMigrationSuffixes = Arrays.asList(".sql");
         model.oracleSqlplus = false;
         model.oracleSqlplusWarn = false;
         model.cleanDisabled = true;
         model.cleanOnValidationError = false;
         model.locations = new ArrayList<>(Collections.singletonList("db/migration"));
         model.target = "latest";
         model.table = "flyway_schema_history";
         model.failOnMissingTarget = false;
         model.cherryPick = new ArrayList<>();
         model.placeholderReplacement = true;
         model.ignoreMigrationPatterns = Arrays.asList("*:future");
         model.validateMigrationNaming = false;
         model.validateOnMigrate = true;
         model.baselineVersion = "1";
         model.baselineDescription = "<< Flyway Baseline >>";
         model.baselineOnMigrate = false;
         model.outOfOrder = false;
         model.skipExecutingMigrations = false;
         model.callbacks = new ArrayList<>();
         model.skipDefaultCallbacks = false;
         model.migrationResolvers = new ArrayList<>();
         model.skipDefaultResolvers = false;
         model.mixed = false;
         model.group = false;
         model.createSchemas = true;
         model.errorOverrides = new ArrayList<>();
         model.stream = false;
         model.batch = false;
         model.outputQueryResults = true;
         model.lockRetryCount = 50;
         model.kerberosConfigFile = "";
         model.oracleKerberosCacheFile = "";
         model.failOnMissingLocations = false;
         model.loggers = Arrays.asList("auto");
         model.placeholders = new HashMap<>();
         return model;
    }

    public FlywayModel merge(FlywayModel otherPojo) {
        FlywayModel result = new FlywayModel();
        result.encoding = encoding.merge(otherPojo.encoding);
        result.environment = environment.merge(otherPojo.environment);
        result.detectEncoding = detectEncoding.merge(otherPojo.detectEncoding);
        result.placeholderPrefix = placeholderPrefix.merge(otherPojo.placeholderPrefix);
        result.placeholderSuffix = placeholderSuffix.merge(otherPojo.placeholderSuffix);
        result.placeholderSeparator = placeholderSeparator.merge(otherPojo.placeholderSeparator);
        result.scriptPlaceholderPrefix = scriptPlaceholderPrefix.merge(otherPojo.scriptPlaceholderPrefix);
        result.scriptPlaceholderSuffix = scriptPlaceholderSuffix.merge(otherPojo.scriptPlaceholderSuffix);
        result.sqlMigrationPrefix = sqlMigrationPrefix.merge(otherPojo.sqlMigrationPrefix);
        result.undoSqlMigrationPrefix = undoSqlMigrationPrefix.merge(otherPojo.undoSqlMigrationPrefix);
        result.executeInTransaction = executeInTransaction.merge(otherPojo.executeInTransaction);
        result.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix.merge(otherPojo.repeatableSqlMigrationPrefix);
        result.sqlMigrationSeparator = sqlMigrationSeparator.merge(otherPojo.sqlMigrationSeparator);
        result.sqlMigrationSuffixes = sqlMigrationSuffixes.merge(otherPojo.sqlMigrationSuffixes);
        result.licenseKey = licenseKey.merge(otherPojo.licenseKey);
        result.oracleSqlplus = oracleSqlplus.merge(otherPojo.oracleSqlplus);
        result.oracleSqlplusWarn = oracleSqlplusWarn.merge(otherPojo.oracleSqlplusWarn);
        result.cleanDisabled = cleanDisabled.merge(otherPojo.cleanDisabled);
        result.cleanOnValidationError = cleanOnValidationError.merge(otherPojo.cleanOnValidationError);
        result.locations = locations.merge(otherPojo.locations);
        result.table = table.merge(otherPojo.table);
        result.tablespace = tablespace.merge(otherPojo.tablespace);
        result.target = target.merge(otherPojo.target);
        result.failOnMissingTarget = failOnMissingTarget.merge(otherPojo.failOnMissingTarget);
        result.cherryPick = cherryPick.merge(otherPojo.cherryPick);
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
        result.oracleKerberosCacheFile = oracleKerberosCacheFile.merge(otherPojo.oracleKerberosCacheFile);
        result.oracleWalletLocation = oracleWalletLocation.merge(otherPojo.oracleWalletLocation);
        result.failOnMissingLocations = failOnMissingLocations.merge(otherPojo.failOnMissingLocations);
        result.loggers = loggers.merge(otherPojo.loggers);
        result.defaultSchema = defaultSchema.merge(otherPojo.defaultSchema);
        result.placeholders = placeholders.merge(otherPojo.placeholders);
        result.propertyResolvers = MergeUtils.merge(propertyResolvers, otherPojo.propertyResolvers, (a,b) -> b != null ? b : a); // TODO: more granular merge
        return result;
    }
}