/*-
 * ========================LICENSE_START=================================
 * flyway-verb-utils
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
package org.flywaydb.verb;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import lombok.CustomLog;
import lombok.Value;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.ExperimentalDatabasePluginResolverImpl;
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
    public static MigrationInfo[] getMigrationInfos(final Configuration configuration,
        final ExperimentalDatabase experimentalDatabase,
        final SchemaHistoryModel schemaHistoryModel) {
        final ParsingContext parsingContext = new ParsingContext();
        parsingContext.populate(experimentalDatabase, configuration);

        final ExperimentalMigrationScannerManager scannerManager = new ExperimentalMigrationScannerManager(configuration);
        final Collection<LoadableResourceMetadata> resources = scannerManager.scan(configuration, parsingContext);
        final MigrationInfo[] migrations = getMigrations(schemaHistoryModel, resources.toArray(LoadableResourceMetadata[]::new),
            configuration);
        return migrations;
    }

    public static SchemaHistoryModel getSchemaHistoryModel(final Configuration configuration,
        final ExperimentalDatabase experimentalDatabase) {
        final SchemaHistoryModel schemaHistoryModel = experimentalDatabase.getSchemaHistoryModel(configuration.getTable());
        if (!experimentalDatabase.schemaHistoryTableExists(configuration.getTable())) {
            LOG.info("Schema history table " + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(),  configuration.getTable()) + " does not exist yet");
        }
        return schemaHistoryModel;
    }

    public static ExperimentalDatabase getExperimentalDatabase(final Configuration configuration) throws SQLException {
        final ExperimentalDatabasePluginResolverImpl experimentalDatabasePluginResolver = new ExperimentalDatabasePluginResolverImpl(configuration.getPluginRegister());
        final Optional<ExperimentalDatabase> resolvedExperimentalDatabase = experimentalDatabasePluginResolver.resolve(configuration.getUrl());
        if (resolvedExperimentalDatabase.isEmpty()) {
            throw new FlywayException("No experimental database plugin found for URL: " + configuration.getUrl());
        }
        final ExperimentalDatabase experimentalDatabase = resolvedExperimentalDatabase.get();
        experimentalDatabase.initialize(getResolvedEnvironment(configuration), configuration);
        LOG.info("Database: " + configuration.getUrl() + " (" + experimentalDatabase.getDatabaseMetaData().databaseProductName() + ")");
        return experimentalDatabase;
    }

    @Value
    private static class MigrationKey implements Comparable<MigrationKey> {
        MigrationVersion migrationVersion;
        String description;
        Integer checksum;
        MigrationType migrationType;
        boolean applied;

        @Override
        public int compareTo(final MigrationKey o) {
            return Comparator.comparing((final MigrationKey other) -> {
                    if (migrationVersion != null && other.migrationVersion != null) {
                        return migrationVersion.compareTo(other.migrationVersion);
                    }

                    if (checksum == null || other.checksum == null) {
                        return checksum == null? -1 : 1;
                    }

                    if (migrationVersion == null && other.migrationVersion == null) {
                        return Comparator.comparing(MigrationKey::getDescription)
                            .thenComparing(MigrationKey::getChecksum)
                            .compare(this, other);
                    }

                    return migrationVersion != null ? -1 : 1;
                })
                .thenComparing(migrationKey -> migrationKey.migrationType.name())
                .thenComparing(MigrationKey::isApplied)
                .compare(this, o);
        }
    }

    private static MigrationInfo[] getMigrations(final SchemaHistoryModel schemaHistoryModel, final LoadableResourceMetadata[] sortedMigrations, final Configuration configuration) {
        final Map<MigrationKey, Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations = new TreeMap<>();
        final MigrationTypeResolver migrationTypeResolver = new CompositeMigrationTypeResolver();

        final List<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems = getResolvedSchemaHistoryItems(schemaHistoryModel,
            configuration,
            migrationTypeResolver);
        final List<LoadableResourceMetadata> resolvedMigrations = getResolvedMigrations(sortedMigrations,
            configuration);

        insertResolvedSchemaHistoryItems(resolvedSchemaHistoryItems, migrations);
        insertResolvedMigrations(resolvedMigrations, migrations);
        insertUndoneMigrations(resolvedSchemaHistoryItems, resolvedMigrations, migrations);
        insertPendingRepeatables(resolvedSchemaHistoryItems, resolvedMigrations, migrations);

        final ExperimentalMigrationComparator comparator = getOrderComparator(configuration);

        final List<ExperimentalMigrationStateCalculator> stateCalculators = getMigrationStateCalculators(
            configuration);

        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> copy = migrations.values().stream().toList();
        return migrations
            .values()
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
        final Map<? super MigrationKey, ? super Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        resolvedSchemaHistoryItems.stream()
            .filter(item -> item.getType().isUndo() && item.isSuccess())
            .map(undoneSchemaHistoryItem -> findOriginalMigration(undoneSchemaHistoryItem, resolvedMigrations))
            .filter(Optional::isPresent)
            .forEach(originalMigration -> {
                final MigrationKey key = new MigrationKey(originalMigration.get().version(),
                    originalMigration.get().description(),
                    originalMigration.get().checksum(),
                    originalMigration.get().migrationType(),
                    false);
                migrations.put(key, Pair.of(null, originalMigration.get()));
            });
    }

    private static void insertPendingRepeatables(final Collection<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems,
        final Collection<LoadableResourceMetadata> resolvedMigrations,
        final Map<? super MigrationKey, ? super Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        resolvedMigrations.stream()
            .filter(migration -> migration.version() == null)
            .forEach(migration -> {
                resolvedSchemaHistoryItems.stream()
                    .filter(ResolvedSchemaHistoryItem::isRepeatable)
                    .filter(schemaHistoryItem -> schemaHistoryItem.getDescription().equals(migration.description()))
                    .max(Comparator.comparing(ResolvedSchemaHistoryItem::getInstalledRank))
                    .ifPresent(schemaHistoryItem -> {
                        if(migration.checksum() != schemaHistoryItem.getChecksum()) {
                            final MigrationKey key = new MigrationKey(null,
                                migration.description(),
                                migration.checksum(),
                                migration.migrationType(),
                                false);
                            migrations.put(key, Pair.of(null, migration));
                        }
                    });
            });
    }

    private static Optional<LoadableResourceMetadata> findOriginalMigration(final ResolvedSchemaHistoryItem undoneSchemaHistoryItem,
        final Collection<LoadableResourceMetadata> resolvedMigrations) {
        return resolvedMigrations.stream()
            .filter(migration -> !migration.migrationType().isUndo())
            .filter(migration -> migration.version().equals(undoneSchemaHistoryItem.getVersion()))
            .findFirst();
    }

    private static LoadableResourceMetadata getTypedMigration(final Configuration configuration,
        final LoadableResourceMetadata sortedMigration) {
        return new LoadableResourceMetadata(sortedMigration.version(),
            sortedMigration.description(),
            sortedMigration.prefix(),
            sortedMigration.loadableResource(),
            sortedMigration.sqlScriptMetadata(),
            sortedMigration.checksum(),
            getMigrationType(sortedMigration.loadableResource(), configuration));
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
            .getPlugins(ExperimentalMigrationStateCalculator.class);
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
        final List<LoadableResourceMetadata> resolvedMigrations = Arrays
            .stream(sortedMigrations)
            .map(sortedMigration -> getTypedMigration(configuration, sortedMigration))
            .toList();
        return resolvedMigrations;
    }

    private static void insertResolvedSchemaHistoryItems(final List<ResolvedSchemaHistoryItem> resolvedSchemaHistoryItems,
        final Map<MigrationKey, Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        for(final ResolvedSchemaHistoryItem schemaHistoryItem : resolvedSchemaHistoryItems) {
            migrations.put(
                new MigrationKey(schemaHistoryItem.getVersion(), schemaHistoryItem.getDescription(), schemaHistoryItem.getChecksum(), schemaHistoryItem.getType(), true),
                Pair.of(schemaHistoryItem, null));
        }
    }

    private static void insertResolvedMigrations(final Iterable<LoadableResourceMetadata> resolvedMigrations,
        final Map<? super MigrationKey, Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
        resolvedMigrations.forEach(typedMigration -> {
            final MigrationKey appliedKey = new MigrationKey(typedMigration.version(),
                typedMigration.description(),
                typedMigration.checksum(),
                typedMigration.migrationType(),
                true);
            if (migrations.containsKey(appliedKey)) {
                migrations.put(appliedKey, Pair.of(migrations.get(appliedKey).getLeft(), typedMigration));
            } else {
                migrations.put(new MigrationKey(typedMigration.version(),
                        typedMigration.description(),
                        typedMigration.checksum(),
                        typedMigration.migrationType(),
                        false),
                    Pair.of(null, typedMigration));
            }
        });
    }

    public static String[] getAllSchemasFromConfiguration(Configuration configuration) {
        if (configuration.getSchemas().length > 0) {
            return configuration.getSchemas();
        } else {
            return new String[]{configuration.getDefaultSchema()};
        }
    }

}
