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

@Getter
@Setter
@NoArgsConstructor
@ExtensionMethod(MergeUtils.class)
public class FlywayModel extends FlywayEnvironmentModel {

    private String environment;
    private String outputType;
    private Boolean outputProgress;
    private Boolean outputLogsInJson;
    private String provisionMode;
    private String color;

    public static FlywayModel defaults(){
        FlywayModel model = new FlywayModel();
        model.environment = "default";
        model.setOutputProgress(false);
        model.setReportFilename(DEFAULT_REPORT_FILENAME);
        model.setDetectEncoding(false);
        model.setEncoding("UTF-8");
        model.setExecuteInTransaction(true);
        model.setPlaceholderPrefix("${");
        model.setPlaceholderSuffix("}");
        model.setPlaceholderSeparator(":");
        model.setScriptPlaceholderPrefix("FP__");
        model.setScriptPlaceholderSuffix("__");
        model.setSqlMigrationPrefix("V");
        model.setRepeatableSqlMigrationPrefix("R");
        model.setSqlMigrationSeparator("__");
        model.setSqlMigrationSuffixes(Arrays.asList(".sql"));
        model.setCleanDisabled(true);
        model.setCleanOnValidationError(false);
        model.setCommunityDBSupportEnabled(true);
        model.setLocations(new ArrayList<>(Collections.singletonList("db/migration")));
        model.setCallbackLocations(Collections.emptyList());
        model.setJarDirs(new ArrayList<>());
        model.setTarget("latest");
        model.setTable("flyway_schema_history");
        model.setFailOnMissingTarget(false);
        model.setPlaceholderReplacement(true);
        model.setIgnoreMigrationPatterns(Arrays.asList("*:future"));
        model.setValidateMigrationNaming(false);
        model.setValidateOnMigrate(true);
        model.setBaselineDescription("<< Flyway Baseline >>");
        model.setBaselineOnMigrate(false);
        model.setOutOfOrder(false);
        model.setSkipExecutingMigrations(false);
        model.setCallbacks(new ArrayList<>());
        model.setSkipDefaultCallbacks(false);
        model.setMigrationResolvers(new ArrayList<>());
        model.setSkipDefaultResolvers(false);
        model.setMixed(false);
        model.setGroup(false);
        model.setCreateSchemas(true);
        model.setErrorOverrides(new ArrayList<>());
        model.setStream(false);
        model.setBatch(false);
        model.setOutputQueryResults(true);
        model.setLockRetryCount(50);
        model.setKerberosConfigFile("");
        model.setFailOnMissingLocations(false);
        model.setLoggers(Arrays.asList("auto"));
        model.setPlaceholders(new HashMap<>());
        model.setProvisionMode("provision");
        model.setReportEnabled(false);
        model.setColor("auto");
        return model;
    }

    @SuppressWarnings("unused") // Backwards compatibility for old property name
    public void setEnvironmentProvisionMode(String mode) {
        this.provisionMode = mode;
    }

    public FlywayModel merge(FlywayModel otherPojo) {
        FlywayModel result = new FlywayModel();
        result.environment = environment.merge(otherPojo.environment);
        result.outputProgress = outputProgress.merge(otherPojo.outputProgress);
        result.outputLogsInJson = outputLogsInJson.merge(otherPojo.outputLogsInJson);
        result.outputType = outputType.merge(otherPojo.outputType);
        result.provisionMode = provisionMode.merge(otherPojo.provisionMode);
        result.color = color.merge(otherPojo.color);
        result.setReportFilename(getReportFilename().merge(otherPojo.getReportFilename()));
        result.setEncoding(getEncoding().merge(otherPojo.getEncoding()));
        result.setDetectEncoding(getDetectEncoding().merge(otherPojo.getDetectEncoding()));
        result.setPlaceholderPrefix(getPlaceholderPrefix().merge(otherPojo.getPlaceholderPrefix()));
        result.setPlaceholderSuffix(getPlaceholderSuffix().merge(otherPojo.getPlaceholderSuffix()));
        result.setPlaceholderSeparator(getPlaceholderSeparator().merge(otherPojo.getPlaceholderSeparator()));
        result.setScriptPlaceholderPrefix(getScriptPlaceholderPrefix().merge(otherPojo.getScriptPlaceholderPrefix()));
        result.setScriptPlaceholderSuffix(getScriptPlaceholderSuffix().merge(otherPojo.getScriptPlaceholderSuffix()));
        result.setPowershellExecutable(getPowershellExecutable().merge(otherPojo.getPowershellExecutable()));
        result.setSqlMigrationPrefix(getSqlMigrationPrefix().merge(otherPojo.getSqlMigrationPrefix()));
        result.setExecuteInTransaction(getExecuteInTransaction().merge(otherPojo.getExecuteInTransaction()));
        result.setRepeatableSqlMigrationPrefix(getRepeatableSqlMigrationPrefix().merge(otherPojo.getRepeatableSqlMigrationPrefix()));
        result.setSqlMigrationSeparator(getSqlMigrationSeparator().merge(otherPojo.getSqlMigrationSeparator()));
        result.setSqlMigrationSuffixes(getSqlMigrationSuffixes().merge(otherPojo.getSqlMigrationSuffixes()));
        result.setCleanDisabled(getCleanDisabled().merge(otherPojo.getCleanDisabled()));
        result.setCleanOnValidationError(getCleanOnValidationError().merge(otherPojo.getCleanOnValidationError()));
        result.setCommunityDBSupportEnabled(getCommunityDBSupportEnabled().merge(otherPojo.getCommunityDBSupportEnabled()));
        result.setLocations(getLocations().merge(otherPojo.getLocations()));
        result.setCallbackLocations(getCallbackLocations().merge(otherPojo.getCallbackLocations()));
        result.setJarDirs(getJarDirs().merge(otherPojo.getJarDirs()));
        result.setTable(getTable().merge(otherPojo.getTable()));
        result.setTablespace(getTablespace().merge(otherPojo.getTablespace()));
        result.setTarget(getTarget().merge(otherPojo.getTarget()));
        result.setFailOnMissingTarget(getFailOnMissingTarget().merge(otherPojo.getFailOnMissingTarget()));
        result.setPlaceholderReplacement(getPlaceholderReplacement().merge(otherPojo.getPlaceholderReplacement()));
        result.setIgnoreMigrationPatterns(getIgnoreMigrationPatterns().merge(otherPojo.getIgnoreMigrationPatterns()));
        result.setValidateMigrationNaming(getValidateMigrationNaming().merge(otherPojo.getValidateMigrationNaming()));
        result.setValidateOnMigrate(getValidateOnMigrate().merge(otherPojo.getValidateOnMigrate()));
        result.setBaselineVersion(getBaselineVersion().merge(otherPojo.getBaselineVersion()));
        result.setBaselineDescription(getBaselineDescription().merge(otherPojo.getBaselineDescription()));
        result.setBaselineOnMigrate(getBaselineOnMigrate().merge(otherPojo.getBaselineOnMigrate()));
        result.setOutOfOrder(getOutOfOrder().merge(otherPojo.getOutOfOrder()));
        result.setSkipExecutingMigrations(getSkipExecutingMigrations().merge(otherPojo.getSkipExecutingMigrations()));
        result.setCallbacks(getCallbacks().merge(otherPojo.getCallbacks()));
        result.setSkipDefaultCallbacks(getSkipDefaultCallbacks().merge(otherPojo.getSkipDefaultCallbacks()));
        result.setMigrationResolvers(getMigrationResolvers().merge(otherPojo.getMigrationResolvers()));
        result.setSkipDefaultResolvers(getSkipDefaultResolvers().merge(otherPojo.getSkipDefaultResolvers()));
        result.setMixed(getMixed().merge(otherPojo.getMixed()));
        result.setGroup(getGroup().merge(otherPojo.getGroup()));
        result.setInstalledBy(getInstalledBy().merge(otherPojo.getInstalledBy()));
        result.setCreateSchemas(getCreateSchemas().merge(otherPojo.getCreateSchemas()));
        result.setErrorOverrides(getErrorOverrides().merge(otherPojo.getErrorOverrides()));
        result.setDryRunOutput(getDryRunOutput().merge(otherPojo.getDryRunOutput()));
        result.setStream(getStream().merge(otherPojo.getStream()));
        result.setBatch(getBatch().merge(otherPojo.getBatch()));
        result.setOutputQueryResults(getOutputQueryResults().merge(otherPojo.getOutputQueryResults()));
        result.setLockRetryCount(getLockRetryCount().merge(otherPojo.getLockRetryCount()));
        result.setKerberosConfigFile(getKerberosConfigFile().merge(otherPojo.getKerberosConfigFile()));
        result.setFailOnMissingLocations(getFailOnMissingLocations().merge(otherPojo.getFailOnMissingLocations()));
        result.setLoggers(getLoggers().merge(otherPojo.getLoggers()));
        result.setDefaultSchema(getDefaultSchema().merge(otherPojo.getDefaultSchema()));
        result.setReportEnabled(getReportEnabled().merge(otherPojo.getReportEnabled()));
        result.setPlaceholders(MergeUtils.merge(getPlaceholders(),otherPojo.getPlaceholders(), (a,b) -> b != null ? b : a));
        result.setPropertyResolvers(MergeUtils.merge(getPropertyResolvers(),otherPojo.getPropertyResolvers(), (a,b) -> b != null ? b : a)); // TODO: more granular merge
        result.setPluginConfigurations(MergeUtils.merge(getPluginConfigurations(), otherPojo.getPluginConfigurations(), MergeUtils::mergeObjects));
        return result;
    }
}
