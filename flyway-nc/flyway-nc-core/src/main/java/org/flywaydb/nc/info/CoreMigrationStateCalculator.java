/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.info;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.nc.NativeConnectorsStateCalculator;
import org.flywaydb.core.internal.nc.schemahistory.ResolvedSchemaHistoryItem;
import org.flywaydb.core.internal.util.Pair;

public class CoreMigrationStateCalculator implements NativeConnectorsStateCalculator {
    private Collection<?> summarizedMigrations;
    private Optional<MigrationVersion> baselineVersion;
    private boolean baselinedSchema;
    private MigrationVersion highestSHTVersion;
    private MigrationVersion highestLocalVersion;
    private MigrationVersion lowestLocalVersion;
    private Map<MigrationVersion, Integer> maxUndoRank;
    private Set<MigrationVersion> deletedVersions;
    private Set<String> deletedDescriptions;
    private Map<String, Integer> maxRepeatableRank;
    private Set<String> pendingRepeatables;
    private NavigableMap<Integer, MigrationVersion> maxVersionUpToRank;

    public MigrationState calculateState(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations,
        final Configuration configuration) {
        if (summarizedMigrations != sortedMigrations) {
            summarize(sortedMigrations);
        }
        if (migration.getLeft() == null) {
            return calculateNoSHTStates(migration, sortedMigrations, configuration);
        }

        return calculateSHTStates(migration, sortedMigrations);
    }

    private MigrationState calculateNoSHTStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations,
        final Configuration configuration) {
        if (baselineVersion.isEmpty() || migration.getRight().isRepeatable() || migration.getRight()
            .version()
            .isNewerThan(baselineVersion.get())) {
            final MigrationVersion target = configuration.getTarget();
            if (migration.getRight().isRepeatable()) {
                return MigrationState.PENDING;
            }
            if (target != null && migration.getRight().version().isNewerThan(target)) {
                return MigrationState.ABOVE_TARGET;
            }

            if (migration.getRight().migrationType().isUndo()) {
                return MigrationState.AVAILABLE;
            }

            if (migration.getRight().sqlScriptMetadata() != null && !migration.getRight()
                .sqlScriptMetadata()
                .shouldExecute()) {
                return MigrationState.IGNORED;
            }

            if (migration.getRight().migrationType().isBaseline() && baselinedSchema) {
                return MigrationState.IGNORED;
            }

            if (!configuration.isOutOfOrder()) {
                if (migration.getRight().version().isNewerThan(highestSHTVersion)) {
                    return MigrationState.PENDING;
                }
                return MigrationState.IGNORED;
            }

            return MigrationState.PENDING;
        } else if (migration.getRight().version().equals(baselineVersion.get())) {
            return migration.getRight().migrationType().isBaseline() && !baselinedSchema
                ? MigrationState.PENDING
                : MigrationState.BASELINE_IGNORED;
        } else {
            return MigrationState.BELOW_BASELINE;
        }
    }

    private MigrationState calculateSHTStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        if (migration.getLeft().getType() == CoreMigrationType.SCHEMA) {
            return MigrationState.SUCCESS;
        }

        if (migration.getLeft().getType().isBaseline()) {
            return migration.getLeft().isSuccess() ? MigrationState.BASELINE : MigrationState.FAILED;
        }

        if (migration.getLeft().isSuccess()) {
            final MigrationState lookAheadState = calculateLookAheadStates(migration, sortedMigrations);
            if (lookAheadState != null) {
                return lookAheadState;
            }

            if (migration.getRight() != null) {
                return MigrationState.SUCCESS;
            }

            if (migration.getLeft().isVersioned()) {
                final MigrationState missingState = calculateMissingStates(migration, sortedMigrations);
                if (missingState != null) {
                    return missingState;
                }
            }

            if (migration.getLeft().isRepeatable() && migration.getLeft().isSuccess()) {
                final MigrationState repeatableState = calculateRepeatableStates(migration, sortedMigrations);
                if (repeatableState != null) {
                    return repeatableState;
                }
            }

            return MigrationState.SUCCESS;
        }
        if (migration.getRight() == null) {
            final MigrationVersion maxLocalVersion = highestLocalVersion;
            if (migration.getLeft().isRepeatable()) {
                return MigrationState.MISSING_FAILED;
            }
            return migration.getLeft().getVersion().isNewerThan(maxLocalVersion)
                ? MigrationState.FUTURE_FAILED
                : MigrationState.MISSING_FAILED;
        }
        return MigrationState.FAILED;
    }

    private static MigrationVersion highestLocalVersion(final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        return sortedMigrations.stream()
            .filter(x -> x.getRight() != null)
            .map(Pair::getRight)
            .filter(LoadableResourceMetadata::isVersioned)
            .filter(x -> !x.migrationType().isUndo())
            .map(LoadableResourceMetadata::version)
            .max(Comparator.naturalOrder())
            .orElse(MigrationVersion.EMPTY);
    }

    private MigrationVersion highestSHTVersion(final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        return sortedMigrations.stream()
            .filter(x -> x.getLeft() != null)
            .filter(x -> !hasFutureUndo(x, sortedMigrations))
            .map(Pair::getLeft)
            .filter(ResolvedSchemaHistoryItem::isVersioned)
            .filter(x -> !x.getType().isUndo())
            .map(ResolvedSchemaHistoryItem::getVersion)
            .max(Comparator.naturalOrder())
            .orElse(MigrationVersion.EMPTY);
    }

    private MigrationState calculateLookAheadStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        if (!migration.getLeft().getType().isUndo() && hasFutureUndo(migration, sortedMigrations)) {
            return MigrationState.UNDONE;
        }

        final boolean futureDelete = migration.getLeft().isRepeatable()
            ? deletedDescriptions.contains(migration.getLeft().getDescription())
            : deletedVersions.contains(migration.getLeft().getVersion());
        if (futureDelete && migration.getLeft().getType() != CoreMigrationType.DELETE) {
            return MigrationState.DELETED;
        }

        if (migration.getLeft().isVersioned() && !migration.getLeft().getType().isUndo()) {
            final Map.Entry<Integer, MigrationVersion> lowerRank = maxVersionUpToRank.lowerEntry(migration.getLeft()
                .getInstalledRank());
            final boolean outOfOrder = lowerRank != null && lowerRank.getValue()
                .isNewerThan(migration.getLeft().getVersion());
            if (outOfOrder) {
                return MigrationState.OUT_OF_ORDER;
            }
        }
        return null;
    }

    private boolean hasFutureUndo(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        final Integer undoRank = maxUndoRank.get(migration.getLeft().getVersion());
        return undoRank != null && undoRank > migration.getLeft().getInstalledRank();
    }

    private MigrationState calculateMissingStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        final MigrationVersion latestLocalVersion = lowestLocalVersion;

        if (migration.getLeft().getVersion().isNewerThan(latestLocalVersion)) {
            return MigrationState.FUTURE_SUCCESS;
        }

        if (latestLocalVersion.isNewerThan(migration.getLeft().getVersion())) {
            return MigrationState.MISSING_SUCCESS;
        }
        return null;
    }

    private MigrationState calculateRepeatableStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        final Integer latestRank = maxRepeatableRank.get(migration.getLeft().getDescription());
        final boolean superseded = latestRank != null && latestRank > migration.getLeft().getInstalledRank();
        if (superseded) {
            return MigrationState.SUPERSEDED;
        }

        final boolean outdated = pendingRepeatables.contains(migration.getLeft().getDescription());

        if (outdated) {
            return MigrationState.OUTDATED;
        }

        if (migration.getRight() == null) {
            return MigrationState.MISSING_SUCCESS;
        }
        return null;
    }

    private void summarize(final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        summarizedMigrations = sortedMigrations;
        maxUndoRank = new HashMap<>();
        deletedVersions = new HashSet<>();
        deletedDescriptions = new HashSet<>();
        maxRepeatableRank = new HashMap<>();
        pendingRepeatables = new HashSet<>();
        maxVersionUpToRank = new TreeMap<>();

        for (final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> x : sortedMigrations) {
            final ResolvedSchemaHistoryItem item = x.getLeft();

            if (item == null) {
                if (x.getRight().isRepeatable()) {
                    pendingRepeatables.add(x.getRight().description());
                }
                continue;
            }

            if (item.getType().isUndo()) {
                maxUndoRank.merge(item.getVersion(), item.getInstalledRank(), Math::max);
            } else if (item.isVersioned()) {
                maxVersionUpToRank.merge(item.getInstalledRank(), item.getVersion(), CoreMigrationStateCalculator::max);
            }

            if (item.getType() == CoreMigrationType.DELETE) {
                if (item.isRepeatable()) {
                    deletedDescriptions.add(item.getDescription());
                } else {
                    deletedVersions.add(item.getVersion());
                }
            }

            if (item.isSuccess() && item.isRepeatable()) {
                maxRepeatableRank.merge(item.getDescription(), item.getInstalledRank(), Math::max);
            }
        }

        MigrationVersion highest = null;
        for (final Map.Entry<Integer, MigrationVersion> entry : maxVersionUpToRank.entrySet()) {
            highest = max(highest, entry.getValue());
            entry.setValue(highest);
        }

        baselineVersion = sortedMigrations.stream()
            .filter(x -> x.getLeft() != null)
            .filter(x -> x.getLeft().getType().isBaseline())
            .map(x -> x.getLeft().getVersion())
            .findFirst();
        baselinedSchema = baselineVersion.isPresent();
        if (baselineVersion.isEmpty()) {
            baselineVersion = sortedMigrations.stream()
                .filter(x -> x.getRight() != null)
                .filter(x -> x.getRight().migrationType().isBaseline())
                .map(x -> x.getRight().version())
                .max(MigrationVersion::compareTo);
        }

        lowestLocalVersion = sortedMigrations.stream()
            .filter(x -> x.getRight() != null)
            .filter(x -> x.getRight().isVersioned())
            .map(x -> x.getRight().version())
            .sorted()
            .findFirst()
            .orElse(MigrationVersion.EMPTY);
        highestLocalVersion = highestLocalVersion(sortedMigrations);
        highestSHTVersion = highestSHTVersion(sortedMigrations);
    }

    private static MigrationVersion max(final MigrationVersion a, final MigrationVersion b) {
        return a == null || b.isNewerThan(a) ? b : a;
    }
}
