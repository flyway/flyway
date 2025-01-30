/*-
 * ========================LICENSE_START=================================
 * flyway-verb-validate
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import static org.flywaydb.core.experimental.ExperimentalModeUtils.logExperimentalDataTelemetry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.CustomLog;
import java.util.Set;
import java.util.stream.Collectors;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.ValidatePatternUtils;
import org.flywaydb.experimental.callbacks.CallbackManager;
import org.flywaydb.verb.VerbUtils;

@CustomLog
public class ValidateVerbExtension implements VerbExtension {

    @Override
    public boolean handlesVerb(final String verb) {
        return "validate".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration, FlywayTelemetryManager flywayTelemetryManager) {

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            final ExperimentalDatabase experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
            final SchemaHistoryModel schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);

            logExperimentalDataTelemetry(flywayTelemetryManager, experimentalDatabase.getDatabaseMetaData());

            final Collection<LoadableResourceMetadata> resources = VerbUtils.scanForResources(configuration,
                experimentalDatabase);

            CallbackManager callbackManager = new CallbackManager(resources, configuration.isSkipDefaultCallbacks());

            final ParsingContext parsingContext = new ParsingContext();
            parsingContext.populate(experimentalDatabase, configuration);

            callbackManager.handleEvent(Event.BEFORE_VALIDATE, experimentalDatabase, configuration, parsingContext);

            final MigrationInfo[] migrations = VerbUtils.getMigrations(schemaHistoryModel,
                resources.toArray(LoadableResourceMetadata[]::new),
                configuration);

            if (!experimentalDatabase.schemaHistoryTableExists(configuration.getTable())) {
                LOG.info("Schema history table " + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(),  configuration.getTable()) + " does not exist yet");
            }

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

            final List<MigrationInfo> notIgnoredMigrations = VerbUtils.removeIgnoredMigrations(configuration, migrations);
            final List<ValidateOutput> invalidMigrations = getInvalidMigrations(notIgnoredMigrations, configuration);
            if (invalidMigrations.isEmpty()) {
                final int count = migrations.length;
                LOG.info(String.format("Successfully validated %d migration%s (execution time %s)",
                    count,
                    count == 1 ? "" : "s",
                    TimeFormat.format(stopWatch.getTotalTimeMillis())));

                callbackManager.handleEvent(Event.AFTER_VALIDATE, experimentalDatabase, configuration, parsingContext);

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

                return new ValidateResult(VersionPrinter.getVersion(),
                experimentalDatabase.getDatabaseMetaData().databaseName(),
                null,
                true,
                migrations.length,
                invalidMigrations,
                new ArrayList<>());
            }

            callbackManager.handleEvent(Event.AFTER_VALIDATE_ERROR, experimentalDatabase, configuration, parsingContext);

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

    private List<ValidateOutput> getInvalidMigrations(List<MigrationInfo> migrations, Configuration configuration) {
        final boolean pendingIgnored = ValidatePatternUtils.isPendingIgnored(configuration.getIgnoreMigrationPatterns());
        final boolean futureIgnored = ValidatePatternUtils.isFutureIgnored(configuration.getIgnoreMigrationPatterns());
        final MigrationVersion appliedBaselineVersion = getAppliedBaselineVersion(migrations);

        List<ValidateOutput> result = new ArrayList<>();

        result.addAll(getTypeMismatch(migrations, appliedBaselineVersion));
        result.addAll(getChecksumChanged(migrations, pendingIgnored, appliedBaselineVersion));
        result.addAll(getDescriptionChanged(migrations, appliedBaselineVersion));
        result.addAll(getOutdatedRepeatables(migrations, pendingIgnored));

        result.addAll(getMissingAndFutureSuccessMigrations(migrations, futureIgnored));
        result.addAll(getMissingSuccessRepeatables(migrations));
        result.addAll(getFailedVersionedMigrations(migrations, futureIgnored));
        result.addAll(getFailedRepeatableMigrations(migrations));
        result.addAll(getNotIgnoredIgnored(migrations, configuration, result));
        result.addAll(getPendingVersionedMigrations(migrations, pendingIgnored));
        result.addAll(getPendingRepeatableMigrations(migrations, pendingIgnored));
        return result;
    }

    private static MigrationVersion getAppliedBaselineVersion(final List<MigrationInfo> migrations) {
        return migrations.stream().filter(x -> x.getType().isBaseline()).map(MigrationInfo::getVersion).max(MigrationVersion::compareTo).orElse(null);
    }

    private static List<ValidateOutput> getOutdatedRepeatables(final List<MigrationInfo> migrations, boolean pendingIgnored) {
        return migrations.stream()
            .filter(x -> !pendingIgnored)
            .filter(x -> x.getState() == MigrationState.OUTDATED)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.OUTDATED_REPEATABLE_MIGRATION,
                    "Detected outdated resolved repeatable migration that should be re-applied to database: " + x.getDescription())))
            .toList();
    }
    
    private static List<ValidateOutput> getTypeMismatch(final List<MigrationInfo> migrations, final MigrationVersion appliedBaselineVersion) {
        return migrations.stream()
            .filter(x -> !x.isTypeMatching())
            .filter(x -> x.isRepeatable() || x.getVersion().isNewerThan(appliedBaselineVersion))
            .map(ValidateVerbExtension::typeMismatchesToValidateOutput)
            .toList();         
    }

    private static ValidateOutput typeMismatchesToValidateOutput(MigrationInfo mismatch) {
        final StringBuilder errorMessage = new StringBuilder();
        final MigrationVersion version = mismatch.getVersion();
        errorMessage.append("Detected type mismatch for migration version ");
        errorMessage.append(version.getVersion());
        errorMessage.append("\n-> ");
        errorMessage.append("Applied to database on ");
        errorMessage.append(mismatch.getInstalledOn());
        errorMessage.append(" (");
        errorMessage.append(mismatch.getAppliedType().name());
        errorMessage.append(")");
        errorMessage.append("Resolved locally at: ");
        errorMessage.append(mismatch.getPhysicalLocation());
        errorMessage.append(" (");
        errorMessage.append(mismatch.getResolvedType().name());
        errorMessage.append(")");
        errorMessage.append("\nEither revert the changes to the migration, or run repair to update the schema history.");
        return new ValidateOutput(version.getVersion(),
            mismatch.getDescription(),
            mismatch.getPhysicalLocation(),
            new ErrorDetails(CoreErrorCode.TYPE_MISMATCH, errorMessage.toString()));
        
    }

    private static List<ValidateOutput> getChecksumChanged(final List<MigrationInfo> migrations, final boolean pendingIgnored, final MigrationVersion appliedBaselineVersion) {
        return migrations.stream()
            .filter(x -> x.isVersioned() || x.getState() != MigrationState.OUTDATED && pendingIgnored)
            .filter(x -> x.isVersioned() || x.getState() != MigrationState.SUPERSEDED && pendingIgnored)
            .filter(x -> x.isRepeatable() || x.getVersion().compareTo(appliedBaselineVersion) > 0)
            .filter(migrationInfo -> !migrationInfo.isChecksumMatching())
            .map(x -> {
                final String migrationIdentifier = x.isVersioned() ? "version " + x.getVersion().getVersion() : x.getScript();
                return new ValidateOutput(x.isVersioned() ? x.getVersion().getVersion() : "",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.CHECKSUM_MISMATCH,
                    "Migration checksum mismatch for migration " + migrationIdentifier +
                        "\n-> Applied to database : " + x.getAppliedChecksum() +
                        "\n-> Resolved locally    : " + x.getResolvedChecksum() +
                        "\nEither revert the changes to the migration, or run repair to update the schema history."));
            })
            .toList();
    }

    private static List<ValidateOutput> getDescriptionChanged(final List<MigrationInfo> migrations, final MigrationVersion appliedBaselineVersion) {
        return migrations.stream()
            .filter(MigrationInfo::isVersioned)
            .filter(x -> x.getVersion().compareTo(appliedBaselineVersion) > 0)
            .filter(migrationInfo -> !migrationInfo.isDescriptionMatching())
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.DESCRIPTION_MISMATCH,
                    "Migration description mismatch for migration version " + x.getVersion().getVersion() +
                        "\n-> Applied to database : " + x.getAppliedDescription() +
                        "\n-> Resolved locally    : " + x.getResolvedDescription() +
                        "\nEither revert the changes to the migration, or run repair to update the schema history.")))
            .toList();
    }
    
    private static List<ValidateOutput> getMissingAndFutureSuccessMigrations(final List<MigrationInfo> migrations, boolean futureIgnored) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.MISSING_SUCCESS
                || (!futureIgnored && x.getState() == MigrationState.FUTURE_SUCCESS))
            .filter(x -> !x.getState().isResolved())
            .filter(MigrationInfo::isVersioned)
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED,
                    "Detected applied migration not resolved locally: " + x.getVersion().getVersion())))
            .toList();
    }
    
    private static List<ValidateOutput> getMissingSuccessRepeatables(final List<MigrationInfo> migrations) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.MISSING_SUCCESS)
            .filter(MigrationInfo::isRepeatable)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED,
                    "Detected applied migration not resolved locally: " + x.getDescription() + ".")))
            .toList();
    }

    private static List<ValidateOutput> getNotIgnoredIgnored(final List<MigrationInfo> migrations, final Configuration configuration, final List<ValidateOutput> result) {
        final boolean isIgnoredIgnored = configuration.getCherryPick()!= null || ValidatePatternUtils.isIgnoredIgnored(configuration.getIgnoreMigrationPatterns());
        if (isIgnoredIgnored) {
            return List.of();
        }
        final Set<String> errored = result.stream().map(x -> x.version == null ? x.description : x.version).collect(Collectors.toSet());
        return migrations.stream().filter(x -> x.getState() == MigrationState.IGNORED)
            .filter(x -> !x.getType().isBaseline())
            .filter(x -> !x.getType().isUndo())
            .filter(x -> !errored.contains(x.isRepeatable() ? x.getDescription() : x.getVersion().getVersion()))
            .filter(MigrationInfo::isShouldExecute)
            .map(x -> {
                if (x.isVersioned()) {
                    final String errorMessage = "Detected resolved migration not applied to database: "
                        + x.getVersion().getVersion()
                        + ".\nTo ignore this migration, set -ignoreMigrationPatterns='*:ignored'. To allow executing this migration, set -outOfOrder=true.";
                    return Pair.of(x, new ErrorDetails(CoreErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED, errorMessage));
                }
                final String errorMessage = "Detected resolved repeatable migration not applied to database: " + x.getDescription() + ".\nTo ignore this migration, set -ignoreMigrationPatterns='*:ignored'.";
                return Pair.of(x, new ErrorDetails(CoreErrorCode.RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED, errorMessage));
            })
            .map(x -> new ValidateOutput(x.getLeft().getVersion().getVersion(),x.getLeft().getDescription(), x.getLeft().getPhysicalLocation(),x.getRight()))
            .toList();
    }

    private static List<ValidateOutput> getPendingVersionedMigrations(final List<MigrationInfo> migrations, boolean pendingIgnored) {
        return migrations.stream()
            .filter(x -> !pendingIgnored)
            .filter(x -> x.getState() == MigrationState.PENDING)
            .filter(MigrationInfo::isVersioned)
            .map(x -> new ValidateOutput(x.getVersion().getVersion(),
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED,
                    "Detected resolved migration not applied to database: " + x.getVersion().getVersion())))
            .toList();
    }

    private static List<ValidateOutput> getPendingRepeatableMigrations(final List<MigrationInfo> migrations, boolean pendingIgnored) {
        return migrations.stream()
            .filter(x -> !pendingIgnored)
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

    private static List<ValidateOutput> getFailedVersionedMigrations(final List<MigrationInfo> migrations, boolean futureIgnored) {
        return migrations.stream()
            .filter(x -> x.getState() == MigrationState.FAILED
                || x.getState() == MigrationState.MISSING_FAILED
                || (!futureIgnored && x.getState() == MigrationState.FUTURE_FAILED))
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
            .filter(x -> x.getState() == MigrationState.FAILED
                || x.getState() == MigrationState.MISSING_FAILED)
            .filter(MigrationInfo::isRepeatable)
            .map(x -> new ValidateOutput("",
                x.getDescription(),
                x.getPhysicalLocation(),
                new ErrorDetails(CoreErrorCode.FAILED_REPEATABLE_MIGRATION,
                    "Detected failed repeatable migration: " + x.getDescription())))
            .toList();
    }
}
