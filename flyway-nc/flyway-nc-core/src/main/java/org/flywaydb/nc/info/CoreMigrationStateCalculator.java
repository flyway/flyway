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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.nc.NativeConnectorsStateCalculator;
import org.flywaydb.core.internal.nc.schemahistory.ResolvedSchemaHistoryItem;
import org.flywaydb.core.internal.util.Pair;

public class CoreMigrationStateCalculator implements NativeConnectorsStateCalculator {

    private Collection<?> summerizedSortedMigrations;
    private MigrationSetSummary cachedSummary;

    public MigrationState calculateState(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations,
        final Configuration configuration) {
        final MigrationSetSummary summary = summaryFor(sortedMigrations);
        if (migration.getLeft() == null) {
            return calculateNoSHTStates(migration, summary, configuration);
        }

        return calculateSHTStates(migration, summary);
    }

    private MigrationSetSummary summaryFor(final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        if (summerizedSortedMigrations != sortedMigrations) {
            cachedSummary = MigrationSetSummary.of(sortedMigrations);
            summerizedSortedMigrations = sortedMigrations;
        }
        return cachedSummary;
    }

    private static MigrationState calculateNoSHTStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final MigrationSetSummary summary,
        final Configuration configuration) {
        final MigrationVersion baselineVersion = summary.baselineVersion;
        final boolean baselinedSchema = summary.baselinedSchema;

        if (baselineVersion == null || migration.getRight().isRepeatable() || migration.getRight()
            .version()
            .isNewerThan(baselineVersion)) {
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
                if (migration.getRight().version().isNewerThan(summary.highestSHTVersion)) {
                    return MigrationState.PENDING;
                }
                return MigrationState.IGNORED;
            }

            return MigrationState.PENDING;
        } else if (migration.getRight().version().equals(baselineVersion)) {
            return migration.getRight().migrationType().isBaseline() && !baselinedSchema
                ? MigrationState.PENDING
                : MigrationState.BASELINE_IGNORED;
        } else {
            return MigrationState.BELOW_BASELINE;
        }
    }

    private static MigrationState calculateSHTStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final MigrationSetSummary summary) {
        if (migration.getLeft().getType() == CoreMigrationType.SCHEMA) {
            return MigrationState.SUCCESS;
        }

        if (migration.getLeft().getType().isBaseline()) {
            return migration.getLeft().isSuccess() ? MigrationState.BASELINE : MigrationState.FAILED;
        }

        if (migration.getLeft().isSuccess()) {
            final MigrationState lookAheadState = calculateLookAheadStates(migration, summary);
            if (lookAheadState != null) {
                return lookAheadState;
            }

            if (migration.getRight() != null) {
                return MigrationState.SUCCESS;
            }

            if (migration.getLeft().isVersioned()) {
                final MigrationState missingState = calculateMissingStates(migration, summary);
                if (missingState != null) {
                    return missingState;
                }
            }

            if (migration.getLeft().isRepeatable() && migration.getLeft().isSuccess()) {
                final MigrationState repeatableState = calculateRepeatableStates(migration, summary);
                if (repeatableState != null) {
                    return repeatableState;
                }
            }

            return MigrationState.SUCCESS;
        }
        if (migration.getRight() == null) {
            if (migration.getLeft().isRepeatable()) {
                return MigrationState.MISSING_FAILED;
            }
            return migration.getLeft().getVersion().isNewerThan(summary.highestLocalVersion)
                ? MigrationState.FUTURE_FAILED
                : MigrationState.MISSING_FAILED;
        }
        return MigrationState.FAILED;
    }

    private static MigrationState calculateLookAheadStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final MigrationSetSummary summary) {
        final ResolvedSchemaHistoryItem item = migration.getLeft();
        if (!item.getType().isUndo() && summary.hasFutureUndo(item)) {
            return MigrationState.UNDONE;
        }

        if (summary.isDeleted(item) && item.getType() != CoreMigrationType.DELETE) {
            return MigrationState.DELETED;
        }

        if (item.isVersioned() && !item.getType().isUndo() && summary.isOutOfOrder(item)) {
            return MigrationState.OUT_OF_ORDER;
        }
        return null;
    }

    private static MigrationState calculateMissingStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final MigrationSetSummary summary) {
        final MigrationVersion lowestLocalVersion = summary.lowestLocalVersion;

        if (migration.getLeft().getVersion().isNewerThan(lowestLocalVersion)) {
            return MigrationState.FUTURE_SUCCESS;
        }

        if (lowestLocalVersion.isNewerThan(migration.getLeft().getVersion())) {
            return MigrationState.MISSING_SUCCESS;
        }
        return null;
    }

    private static MigrationState calculateRepeatableStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final MigrationSetSummary summary) {
        final ResolvedSchemaHistoryItem item = migration.getLeft();
        if (summary.isSuperseded(item)) {
            return MigrationState.SUPERSEDED;
        }

        if (summary.pendingRepeatableDescriptions.contains(item.getDescription())) {
            return MigrationState.OUTDATED;
        }

        if (migration.getRight() == null) {
            return MigrationState.MISSING_SUCCESS;
        }
        return null;
    }

    /**
     * Cross-migration metadata facts computed in a single pass.
     */
    private static final class MigrationSetSummary {
        private final boolean baselinedSchema;
        private final MigrationVersion baselineVersion;
        private final MigrationVersion highestSHTVersion;
        private final MigrationVersion highestLocalVersion;
        private final MigrationVersion lowestLocalVersion;
        private final Map<MigrationVersion, Integer> maxUndoRankByVersion;
        private final Set<MigrationVersion> deletedVersions;
        private final Set<String> deletedRepeatableDescriptions;
        private final Map<String, Integer> maxSuccessfulRepeatableRankByDescription;
        private final Set<String> pendingRepeatableDescriptions;
        private final Set<ResolvedSchemaHistoryItem> outOfOrderItems;

        private MigrationSetSummary(final boolean baselinedSchema,
            final MigrationVersion baselineVersion,
            final MigrationVersion highestSHTVersion,
            final MigrationVersion highestLocalVersion,
            final MigrationVersion lowestLocalVersion,
            final Map<MigrationVersion, Integer> maxUndoRankByVersion,
            final Set<MigrationVersion> deletedVersions,
            final Set<String> deletedRepeatableDescriptions,
            final Map<String, Integer> maxSuccessfulRepeatableRankByDescription,
            final Set<String> pendingRepeatableDescriptions,
            final Set<ResolvedSchemaHistoryItem> outOfOrderItems) {
            this.baselinedSchema = baselinedSchema;
            this.baselineVersion = baselineVersion;
            this.highestSHTVersion = highestSHTVersion;
            this.highestLocalVersion = highestLocalVersion;
            this.lowestLocalVersion = lowestLocalVersion;
            this.maxUndoRankByVersion = maxUndoRankByVersion;
            this.deletedVersions = deletedVersions;
            this.deletedRepeatableDescriptions = deletedRepeatableDescriptions;
            this.maxSuccessfulRepeatableRankByDescription = maxSuccessfulRepeatableRankByDescription;
            this.pendingRepeatableDescriptions = pendingRepeatableDescriptions;
            this.outOfOrderItems = outOfOrderItems;
        }

        private static MigrationSetSummary of(final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> migrations) {
            ResolvedSchemaHistoryItem baselineItem = null;
            MigrationVersion resolvedBaselineVersion = null;
            MigrationVersion highestLocalVersion = null;
            MigrationVersion lowestLocalVersion = null;
            final Map<MigrationVersion, Integer> maxUndoRankByVersion = new HashMap<>();
            final Set<MigrationVersion> deletedVersions = new HashSet<>();
            final Set<String> deletedRepeatableDescriptions = new HashSet<>();
            final Map<String, Integer> maxSuccessfulRepeatableRankByDescription = new HashMap<>();
            final Set<String> pendingRepeatableDescriptions = new HashSet<>();
            final List<ResolvedSchemaHistoryItem> versionedNonUndoItems = new ArrayList<>();

            for (final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> pair : migrations) {
                final ResolvedSchemaHistoryItem item = pair.getLeft();
                final LoadableResourceMetadata resource = pair.getRight();

                if (item != null) {
                    if (baselineItem == null && item.getType().isBaseline()) {
                        baselineItem = item;
                    }
                    if (item.getType().isUndo() && item.getVersion() != null) {
                        maxUndoRankByVersion.merge(item.getVersion(), item.getInstalledRank(), Math::max);
                    }
                    if (item.getType() == CoreMigrationType.DELETE) {
                        if (item.isRepeatable()) {
                            deletedRepeatableDescriptions.add(item.getDescription());
                        } else {
                            deletedVersions.add(item.getVersion());
                        }
                    }
                    if (item.isSuccess() && item.isRepeatable()) {
                        maxSuccessfulRepeatableRankByDescription.merge(item.getDescription(),
                            item.getInstalledRank(),
                            Math::max);
                    }
                    if (item.isVersioned() && !item.getType().isUndo()) {
                        versionedNonUndoItems.add(item);
                    }
                } else if (resource != null && resource.isRepeatable()) {
                    pendingRepeatableDescriptions.add(resource.description());
                }

                if (resource != null) {
                    if (resource.migrationType().isBaseline()) {
                        resolvedBaselineVersion = higher(resolvedBaselineVersion, resource.version());
                    }
                    if (resource.isVersioned()) {
                        lowestLocalVersion = lower(lowestLocalVersion, resource.version());
                        if (!resource.migrationType().isUndo()) {
                            highestLocalVersion = higher(highestLocalVersion, resource.version());
                        }
                    }
                }
            }

            final boolean baselinedSchema = baselineItem != null && baselineItem.getVersion() != null;

            // A migration is out of order when a newer version was already installed at a lower rank, so a
            // single pass in rank order over the running highest version answers it for every item.
            versionedNonUndoItems.sort(Comparator.comparingInt(ResolvedSchemaHistoryItem::getInstalledRank));
            final Set<ResolvedSchemaHistoryItem> outOfOrderItems = Collections.newSetFromMap(new IdentityHashMap<>());
            MigrationVersion highestVersionSoFar = null;
            for (final ResolvedSchemaHistoryItem item : versionedNonUndoItems) {
                if (highestVersionSoFar != null && highestVersionSoFar.isNewerThan(item.getVersion())) {
                    outOfOrderItems.add(item);
                }
                highestVersionSoFar = higher(highestVersionSoFar, item.getVersion());
            }

            MigrationVersion highestSHTVersion = null;
            for (final ResolvedSchemaHistoryItem item : versionedNonUndoItems) {
                final Integer undoRank = maxUndoRankByVersion.get(item.getVersion());
                if (undoRank == null || undoRank <= item.getInstalledRank()) {
                    highestSHTVersion = higher(highestSHTVersion, item.getVersion());
                }
            }

            return new MigrationSetSummary(baselinedSchema,
                baselinedSchema ? baselineItem.getVersion() : resolvedBaselineVersion,
                orEmpty(highestSHTVersion),
                orEmpty(highestLocalVersion),
                orEmpty(lowestLocalVersion),
                maxUndoRankByVersion,
                deletedVersions,
                deletedRepeatableDescriptions,
                maxSuccessfulRepeatableRankByDescription,
                pendingRepeatableDescriptions,
                outOfOrderItems);
        }

        private boolean hasFutureUndo(final ResolvedSchemaHistoryItem item) {
            if (item.getVersion() == null) {
                return false;
            }
            final Integer undoRank = maxUndoRankByVersion.get(item.getVersion());
            return undoRank != null && undoRank > item.getInstalledRank();
        }

        private boolean isDeleted(final ResolvedSchemaHistoryItem item) {
            return item.isRepeatable()
                ? deletedRepeatableDescriptions.contains(item.getDescription())
                : deletedVersions.contains(item.getVersion());
        }

        private boolean isSuperseded(final ResolvedSchemaHistoryItem item) {
            final Integer maxRank = maxSuccessfulRepeatableRankByDescription.get(item.getDescription());
            return maxRank != null && maxRank > item.getInstalledRank();
        }

        private boolean isOutOfOrder(final ResolvedSchemaHistoryItem item) {
            return outOfOrderItems.contains(item);
        }

        private static MigrationVersion higher(final MigrationVersion current, final MigrationVersion candidate) {
            return current == null || candidate.compareTo(current) > 0 ? candidate : current;
        }

        private static MigrationVersion lower(final MigrationVersion current, final MigrationVersion candidate) {
            return current == null || candidate.compareTo(current) < 0 ? candidate : current;
        }

        private static MigrationVersion orEmpty(final MigrationVersion version) {
            return version == null ? MigrationVersion.EMPTY : version;
        }
    }
}
