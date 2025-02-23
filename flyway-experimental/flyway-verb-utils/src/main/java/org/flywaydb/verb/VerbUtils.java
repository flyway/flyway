/*-
 * ========================LICENSE_START=================================
 * flyway-verb-utils
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
package org.flywaydb.verb;

import static org.flywaydb.core.experimental.ExperimentalModeUtils.resolveExperimentalDatabasePlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.migration.CompositeMigrationTypeResolver;
import org.flywaydb.core.experimental.migration.ExperimentalMigrationComparator;
import org.flywaydb.core.experimental.migration.ExperimentalMigrationScannerManager;
import org.flywaydb.core.experimental.migration.ExperimentalMigrationStateCalculator;
import org.flywaydb.core.experimental.migration.MigrationTypeResolver;
import org.flywaydb.core.experimental.schemahistory.ResolvedSchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.configuration.resolvers.ProvisionerMode;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.verb.info.CoreMigrationStateCalculator;
import org.flywaydb.verb.info.ExperimentalMigrationInfoImpl;

@CustomLog
public class VerbUtils {
    private static boolean databaseInfoPrinted;

    public static Collection<LoadableResourceMetadata> scanForResources(final Configuration configuration,
        final ExperimentalDatabase experimentalDatabase) {
        final ParsingContext parsingContext = new ParsingContext();
        parsingContext.populate(experimentalDatabase, configuration);

        final ExperimentalMigrationScannerManager scannerManager = new ExperimentalMigrationScannerManager(configuration);
        final Collection<LoadableResourceMetadata> resources = scannerManager.scan(configuration, parsingContext);
        return resources;
    }

    public static SchemaHistoryModel getSchemaHistoryModel(final Configuration configuration,
        final ExperimentalDatabase experimentalDatabase) {
        return experimentalDatabase.getSchemaHistoryModel(configuration.getTable());
    }

    public static ExperimentalDatabase getExperimentalDatabase(final Configuration configuration) throws SQLException {
        final Optional<ExperimentalDatabase> resolvedExperimentalDatabase = resolveExperimentalDatabasePlugin(configuration);

        if (resolvedExperimentalDatabase.isEmpty()) {
            throw new FlywayException("No Native Connectors database plugin found for the designated database");
        }

        final ExperimentalDatabase experimentalDatabase = resolvedExperimentalDatabase.get();

        experimentalDatabase.initialize(getResolvedEnvironment(configuration), configuration);
        if (!databaseInfoPrinted) {
            LOG.info("Database: " + experimentalDatabase.redactUrl(configuration.getUrl()) + " (" + experimentalDatabase.getDatabaseMetaData()
                .databaseProductName() + ")");
            databaseInfoPrinted = true;
        }
        return experimentalDatabase;
    }

    public static MigrationInfo[] getMigrations(final SchemaHistoryModel schemaHistoryModel, final LoadableResourceMetadata[] sortedMigrations, final Configuration configuration) {
        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations = new ArrayList<>();
        final MigrationTypeResolver migrationTypeResolver = new CompositeMigrationTypeResolver();

        final List<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems = getResolvedSchemaHistoryItems(schemaHistoryModel,
            configuration,
            migrationTypeResolver);
        final List<LoadableResourceMetadata> resolvedMigrations = getResolvedMigrations(sortedMigrations,
            configuration);

        insertResolvedSchemaHistoryItems(resolvedSchemaHistoryItems, migrations);
        insertResolvedMigrations(resolvedMigrations, migrations);
        insertUndoneMigrations(resolvedSchemaHistoryItems, resolvedMigrations, migrations);

        final ExperimentalMigrationComparator comparator = getOrderComparator(configuration);

        final List<ExperimentalMigrationStateCalculator> stateCalculators = getMigrationStateCalculators(
            configuration);

        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> copy = migrations.stream().toList();
        return migrations
            .stream()
            .map(x -> {
                final MigrationState state = stateCalculators.stream()
                    .map(stateCalculator -> stateCalculator.calculateState(x, copy, configuration))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new FlywayException("No state calculator found"));
                return new ExperimentalMigrationInfoImpl(x, state);
            })
            .filter(x -> x.getType() != CoreMigrationType.DELETE)
            .sorted(comparator.getComparator(configuration))
            .toArray(MigrationInfo[]::new);
    }

    private static List<ResolvedSchemaHistoryItem> getResolvedSchemaHistoryItems(final SchemaHistoryModel schemaHistoryModel,
        final Configuration configuration,
        final MigrationTypeResolver migrationTypeResolver) {
        return schemaHistoryModel.getSchemaHistoryItems()
            .stream()
            .map(schemaHistoryItem -> ResolvedSchemaHistoryItem.builder()
                .version(schemaHistoryItem.getVersion() == null ? null : MigrationVersion.fromVersion(schemaHistoryItem.getVersion()))
                .description(schemaHistoryItem.getDescription())
                .type(migrationTypeResolver.resolveMigrationTypeFromName(schemaHistoryItem.getType(), configuration))
                .script(schemaHistoryItem.getScript())
                .checksum(schemaHistoryItem.getChecksum())
                .installedBy(schemaHistoryItem.getInstalledBy())
                .installedOn(schemaHistoryItem.getInstalledOn())
                .executionTime(schemaHistoryItem.getExecutionTime())
                .success(schemaHistoryItem.isSuccess())
                .installedRank(schemaHistoryItem.getInstalledRank())
                .build())
            .toList();
    }

    private static void insertUndoneMigrations(final Collection<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems,
        final Collection<LoadableResourceMetadata> resolvedMigrations,
        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        resolvedSchemaHistoryItems.stream()
            .filter(item -> item.getType().isUndo() && item.isSuccess())
            .filter(item -> shouldAddUndone(item, resolvedSchemaHistoryItems))
            .map(undoneSchemaHistoryItem -> findOriginalMigration(undoneSchemaHistoryItem, resolvedMigrations))
            .filter(Optional::isPresent)
            .forEach(originalMigration -> {
                migrations.add(Pair.of(null, originalMigration.get()));
            });
    }

    private static Optional<LoadableResourceMetadata> findOriginalMigration(final ResolvedSchemaHistoryItem undoneSchemaHistoryItem,
        final Collection<LoadableResourceMetadata> resolvedMigrations) {
        return resolvedMigrations.stream()
            .filter(LoadableResourceMetadata::isVersioned)
            .filter(migration -> !migration.migrationType().isUndo())
            .filter(migration -> migration.version().equals(undoneSchemaHistoryItem.getVersion()))
            .findFirst();
    }

    private static boolean shouldAddUndone(final ResolvedSchemaHistoryItem undoneSchemaHistoryItem, final Collection<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems) {
        return resolvedSchemaHistoryItems.stream()
            .filter(ResolvedSchemaHistoryItem::isVersioned) 
            .filter(item -> item.getInstalledRank() > undoneSchemaHistoryItem.getInstalledRank())
            .noneMatch(item -> item.getVersion().equals(undoneSchemaHistoryItem.getVersion()) &&
                !item.getType().isUndo() && item.isSuccess());
    }

    private static LoadableResourceMetadata getTypedMigration(final Configuration configuration,
        final LoadableResourceMetadata sortedMigration) {

        MigrationType migrationType = getMigrationType(sortedMigration.loadableResource(), configuration);

        if (migrationType == null) {
            return null;
        }

        return new LoadableResourceMetadata(sortedMigration.version(),
            sortedMigration.description(),
            sortedMigration.prefix(),
            sortedMigration.loadableResource(),
            sortedMigration.sqlScriptMetadata(),
            sortedMigration.checksum(),
            migrationType);
    }

    private static MigrationType getMigrationType(final LoadableResource resource, final Configuration configuration) {
        final CompositeMigrationTypeResolver resolver = new CompositeMigrationTypeResolver();
        return resolver.resolveMigrationType(resource.getFilename(), configuration);
    }

    private static ResolvedEnvironment getResolvedEnvironment(final Configuration configuration) {
        final String envName =  configuration.getCurrentEnvironmentName();
        final String envProvisionMode = configuration.getModernConfig().getFlyway().getProvisionMode();
        final ProvisionerMode provisionerMode = StringUtils.hasText(envProvisionMode) ? ProvisionerMode.fromString(envProvisionMode) : ProvisionerMode.Provision;
        final ResolvedEnvironment resolved = configuration.getResolvedEnvironment(envName, provisionerMode, null);
        if (resolved == null) {
            throw new FlywayException("Environment '" + envName + "' not found. Check that this environment exists in your configuration.");
        }
        return resolved;
    }

    private static List<ExperimentalMigrationStateCalculator> getMigrationStateCalculators(final Configuration configuration) {
        final List<ExperimentalMigrationStateCalculator> stateCalculators = configuration.getPluginRegister()
            .getLicensedPlugins(ExperimentalMigrationStateCalculator.class, configuration);
        stateCalculators.add(new CoreMigrationStateCalculator());
        return stateCalculators;
    }

    private static ExperimentalMigrationComparator getOrderComparator(final Configuration configuration) {
        return configuration.getPluginRegister().getPlugins(ExperimentalMigrationComparator.class).stream().filter(
            comparatorPlugin -> comparatorPlugin.getName().equals("Info")).max(Comparator.comparingInt(
            experimentalMigrationComparator -> experimentalMigrationComparator.getPriority(configuration))).orElseThrow(
            () -> new FlywayException("No Info comparator found"));
    }

    private static List<LoadableResourceMetadata> getResolvedMigrations(final LoadableResourceMetadata[] sortedMigrations,
        final Configuration configuration) {
        return Arrays
            .stream(sortedMigrations)
            .map(sortedMigration -> getTypedMigration(configuration, sortedMigration))
            .filter(Objects::nonNull)
            .toList();
    }

    private static void insertResolvedSchemaHistoryItems(final List<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems,
        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        for(final ResolvedSchemaHistoryItem schemaHistoryItem : resolvedSchemaHistoryItems) {
            migrations.add(Pair.of(schemaHistoryItem, null));
        }
    }

    private static void insertResolvedMigrations(final Iterable<LoadableResourceMetadata> resolvedMigrations,
        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        resolvedMigrations.forEach(resolvedMigration -> {
            List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> matchedMigrations = findMigrationsByResourceMetadata(migrations, resolvedMigration);

            if (!matchedMigrations.isEmpty()) {
                for (Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration: matchedMigrations) {
                    migrations.add(Pair.of(migration.getLeft(), resolvedMigration));
                    migrations.remove(migration);
                }
            } else {
                migrations.add(Pair.of(null, resolvedMigration));
            }
        });
    }

    private static List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> findMigrationsByResourceMetadata(
        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations,
        final LoadableResourceMetadata resourceMetadata) {

        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> result = new ArrayList<>();

        for (final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration: migrations) {
            final ResolvedSchemaHistoryItem item = migration.getLeft();
            if (item != null) {
                final boolean versionMatched = item.isRepeatable() ? item.getDescription().equals(resourceMetadata.description())
                    && item.getChecksum().equals(resourceMetadata.checksum()) :
                    item.getVersion().equals(resourceMetadata.version());
                if (versionMatched && typesCompatible(resourceMetadata, item)) {
                    result.add(migration);
                }
            }
        }

        return result;
    }

    private static boolean typesCompatible(final LoadableResourceMetadata resourceMetadata,
        final ResolvedSchemaHistoryItem item) {
        return item.getType().isBaseline() == resourceMetadata.migrationType().isBaseline()
            && item.getType() != CoreMigrationType.BASELINE
            && item.getType().isUndo() == resourceMetadata.migrationType().isUndo();
    }

    public static String[] getAllSchemasFromConfiguration(Configuration configuration) {
        if (configuration.getSchemas().length > 0) {
            return configuration.getSchemas();
        } else {
            return new String[]{configuration.getDefaultSchema()};
        }
    }

    public static String toMigrationText(final MigrationInfo migration, final boolean isExecuteInTransaction,
        final ExperimentalDatabase database, final boolean outOfOrder) {
        final String migrationText;
        if (migration.getVersion() != null) {
            migrationText = "schema " + database.doQuote(database.getCurrentSchema()) + " to version " + database.doQuote(migration.getVersion()
                + (StringUtils.hasLength(migration.getDescription()) ? " - " + migration.getDescription() : ""))
                + (outOfOrder ? " [out of order]" : "")
                + (isExecuteInTransaction ? "" : " [non-transactional]");
        } else {
            migrationText = "schema " + database.doQuote(database.getCurrentSchema()) + " with repeatable migration " + database.doQuote(migration.getDescription())
                + (isExecuteInTransaction ? "" : " [non-transactional]");
        }
        return migrationText;
    }

    public static List<MigrationInfo> removeIgnoredMigrations(final Configuration configuration,
        final MigrationInfo[] migrations) {
        return Arrays.stream(migrations).filter(x -> Arrays.stream(configuration.getIgnoreMigrationPatterns())
            .noneMatch(pattern -> pattern.matchesMigration(x.isVersioned(), x.getState()))).toList();
    }
    
    public static FlywayTelemetryManager getFlywayTelemetryManager(final Configuration configuration) {
        return configuration.getPluginRegister().getPluginInstanceOf(FlywayTelemetryManager.class);
    }
}
