/*-
 * ========================LICENSE_START=================================
 * flyway-verb-validate
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
package org.flywaydb.verb.validate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.ValidatePatternUtils;
import org.flywaydb.verb.VerbUtils;

@CustomLog
public class ValidateVerbExtension implements VerbExtension {

    @Override
    public boolean handlesVerb(final String verb) {
        return "validate".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            final ExperimentalDatabase experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
            final SchemaHistoryModel schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);

            final MigrationInfo[] migrations = VerbUtils.getMigrationInfos(configuration,
                experimentalDatabase,
                schemaHistoryModel);

            if (!experimentalDatabase.isSchemaExists(experimentalDatabase.getCurrentSchema())) {
                if (migrations.length != 0 && !ValidatePatternUtils.isPendingIgnored(configuration.getIgnoreMigrationPatterns())) {
                    String validationErrorMessage = "Schema " + experimentalDatabase.doQuote(experimentalDatabase.getCurrentSchema())  + " doesn't exist yet";
                    ErrorDetails validationError = new ErrorDetails(CoreErrorCode.SCHEMA_DOES_NOT_EXIST, validationErrorMessage);
                    return new ValidateResult(VersionPrinter.getVersion(),
                        experimentalDatabase.getDatabaseMetaData().databaseName(),
                        validationError,
                        false,
                        0,
                        new ArrayList<>(),
                        new ArrayList<>());
                }
            }
            stopWatch.stop();

            final int count = migrations.length;
            LOG.info(String.format("Successfully validated %d migration%s (execution time %s)",
                count,
                count == 1 ? "" : "s",
                TimeFormat.format(stopWatch.getTotalTimeMillis())));

            if (migrations.length == 0) {
                final ArrayList<String> warnings = new ArrayList<>();
                final String noMigrationsWarning = "No migrations found. Are your locations set up correctly?";
                warnings.add(noMigrationsWarning);
                LOG.warn(noMigrationsWarning);

                return new ValidateResult(VersionPrinter.getVersion(),
                    experimentalDatabase.getDatabaseMetaData().databaseName(),
                    null,
                    true,
                    migrations.length,
                    new ArrayList<>(),
                    warnings);
            }

            final List<MigrationInfo> notIgnoredMigrations = removeIgnoredMigrations(configuration, migrations);
            final List<ValidateOutput> invalidMigrations = getInvalidMigrations(notIgnoredMigrations);
            if (invalidMigrations.isEmpty()) {
                return new ValidateResult(VersionPrinter.getVersion(),
                experimentalDatabase.getDatabaseMetaData().databaseName(),
                null,
                true,
                migrations.length,
                invalidMigrations,
                new ArrayList<>());
            }
            return new ValidateResult(VersionPrinter.getVersion(),
                experimentalDatabase.getDatabaseMetaData().databaseName(),
                new ErrorDetails(CoreErrorCode.VALIDATE_ERROR, "Migrations have failed validation"),
                false,
                0,
                invalidMigrations,
                new ArrayList<>());
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    private List<MigrationInfo> removeIgnoredMigrations(final Configuration configuration,
        final MigrationInfo[] migrations) {
        return Arrays.stream(migrations).filter(x -> Arrays.stream(configuration.getIgnoreMigrationPatterns())
            .noneMatch(pattern -> pattern.matchesMigration(x.isVersioned(), x.getState()))).toList();
    }

    private List<ValidateOutput> getInvalidMigrations(List<MigrationInfo> migrations) {
        List<ValidateOutput> result = new ArrayList<>();
        final List<ValidateOutput> futureFailedMigrations = getFutureFailedMigrations(migrations);
        
        result.addAll(getTypeMismatch(migrations));
        result.addAll(getChecksumChanged(migrations));
        result.addAll(getDescriptionChanged(migrations));
        result.addAll(getOutdatedRepeatables(migrations));
        result.addAll(getFailedVersionedMigrations(migrations));
        result.addAll(getFailedRepeatableMigrations(migrations));
        result.addAll(getPendingVersionedMigrations(migrations));
        result.addAll(getPendingRepeatableMigrations(migrations));
        if(futureFailedMigrations.isEmpty()) {
            result.addAll(getMissingMigrations(migrations));
        }else {
            result.addAll(futureFailedMigrations);
        }
        result.addAll(getMissingRepeatables(migrations));
        return result;
    }

    private static List<ValidateOutput> getOutdatedRepeatables(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.OUTDATED)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.OUTDATED_REPEATABLE_MIGRATION,
                    "Detected outdated resolved repeatable migration that should be re-applied to database: " + x.getDescription())))
            .toList();
    }
    
    private static List<ValidateOutput> getTypeMismatch(final List<MigrationInfo> migrations) {
        final List<MigrationInfo> potentialMigrations = migrations.stream()
            .filter(x -> x.getState() != MigrationState.UNDONE)
            .filter(MigrationInfo::isVersioned)
            .filter(version -> !version.getType().isUndo())
            .toList();
        return potentialMigrations.stream()
            .map(MigrationInfo::getVersion)
            .distinct()
            .map(version -> getMigrationsWithVersion(potentialMigrations, version))
            .filter(x -> x.size() >= 2)
            .map(ValidateVerbExtension::typeMismatchesToValidateOutput)
            .toList();         
    }

    private static List<MigrationInfo> getMigrationsWithVersion(final List<MigrationInfo> migrations, final MigrationVersion version) {
        return migrations.stream()
            .filter(otherVersion -> otherVersion.getVersion().equals(version))
            .toList();
    }

    private static ValidateOutput typeMismatchesToValidateOutput(List<MigrationInfo> matchingVersions) {
        final StringBuilder errorMessage = new StringBuilder();
        final MigrationVersion version = matchingVersions.get(0).getVersion();
        errorMessage.append("Detected type mismatch for migration version ");
        errorMessage.append(version.getVersion());
        matchingVersions.forEach(matchingMigration -> {
            final boolean applied = matchingMigration.getInstalledOn() != null;
            errorMessage.append("\n-> ");
            errorMessage.append(applied ? "Applied to database on " : "Resolved locally at: ");
            errorMessage.append(applied
                ? matchingMigration.getInstalledOn()
                : matchingMigration.getPhysicalLocation());
            errorMessage.append(" (");
            errorMessage.append(matchingMigration.getType().name());
            errorMessage.append(")");
        });
        return new ValidateOutput(version.getVersion(),
            matchingVersions.get(0).getDescription(),
            matchingVersions.get(0).getPhysicalLocation(),
            new ErrorDetails(CoreErrorCode.TYPE_MISMATCH, errorMessage.toString()));
        
    }

    private static List<ValidateOutput> getChecksumChanged(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() != MigrationState.OUTDATED)
            .filter(x -> x.getState() != MigrationState.SUPERSEDED)
            .filter(MigrationInfo::isVersioned)
            .filter(migrationInfo -> !migrationInfo.isChecksumMatching())
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.CHECKSUM_MISMATCH,
                    "Migration checksum mismatch for migration version " + x.getVersion().getVersion() +
                        "\n-> Applied to database : " + x.getAppliedChecksum() +
                        "\n-> Resolved locally    : " + x.getResolvedChecksum())))
            .toList();
    }

    private static List<ValidateOutput> getDescriptionChanged(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(MigrationInfo::isVersioned)
            .filter(migrationInfo -> !migrationInfo.isDescriptionMatching())
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.DESCRIPTION_MISMATCH,
                    "Migration description mismatch for migration version " + x.getVersion().getVersion() +
                        "\n-> Applied to database : " + x.getAppliedDescription() +
                        "\n-> Resolved locally    : " + x.getResolvedDescription())))
            .toList();
    }
    
    private static List<ValidateOutput> getMissingMigrations(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.FUTURE_SUCCESS
                || x.getState() == MigrationState.MISSING_SUCCESS
                || x.getState() == MigrationState.FUTURE_FAILED
                || x.getState() == MigrationState.MISSING_FAILED)
            .filter(x -> !x.getState().isResolved())
            .filter(MigrationInfo::isVersioned)
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED,
                    "Detected applied migration not resolved locally: " + x.getVersion().getVersion())))
            .toList();
    }
    
    private static List<ValidateOutput> getMissingRepeatables(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.FUTURE_SUCCESS
                || x.getState() == MigrationState.MISSING_SUCCESS
                || x.getState() == MigrationState.FUTURE_FAILED
                || x.getState() == MigrationState.MISSING_FAILED)
            .filter(x -> !x.getState().isResolved())
            .filter(MigrationInfo::isRepeatable)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED,
                    "Detected applied migration not resolved locally: " + x.getDescription() + ".")))
            .toList();
    }

    private static List<ValidateOutput> getFutureFailedMigrations(final List<MigrationInfo> migrations) {
        final List<MigrationInfo> futureFailedMigrations = migrations.stream()
            .filter(x -> x.getState() == MigrationState.FUTURE_FAILED)
            .toList();
        return futureFailedMigrations.stream()
            .flatMap(futureFailed -> migrations.stream()
            .filter(x -> !x.getState().isApplied())
            .filter(MigrationInfo::isVersioned)
            .filter(x -> futureFailed.getVersion().isNewerThan(x.getVersion())))
            .distinct()
            .map(x ->
                new ValidateOutput(
                    x.getVersion().getVersion(),
                    x.getDescription(),
                    x.getPhysicalLocation(),
                    new ErrorDetails(CoreErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED,"Detected resolved migration not applied to database: " + x.getVersion().getVersion())
                )).toList();
    }

    private static List<ValidateOutput> getPendingVersionedMigrations(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.PENDING)
            .filter(MigrationInfo::isVersioned)
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED,
                    "Detected resolved migration not applied to database: " + x.getVersion().getVersion())))
            .toList();
    }

    private static List<ValidateOutput> getPendingRepeatableMigrations(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.PENDING)
            .filter(MigrationInfo::isRepeatable)
            .filter(MigrationInfo::isShouldExecute)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED,
                    "Detected resolved repeatable migration not applied to database: " + x.getDescription() + ".")))
            .toList();
    }

    private static List<ValidateOutput> getFailedVersionedMigrations(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.FAILED)
            .filter(MigrationInfo::isVersioned)
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.FAILED_VERSIONED_MIGRATION,
                    "Detected failed migration to version " + x.getVersion().getVersion())))
            .toList();
    }

    private static List<ValidateOutput> getFailedRepeatableMigrations(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.FAILED)
            .filter(MigrationInfo::isRepeatable)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.FAILED_REPEATABLE_MIGRATION,
                    "Detected failed repeatable migration: " + x.getDescription())))
            .toList();
    }
}
