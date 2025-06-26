/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.info;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalMigrationStateCalculator;
import org.flywaydb.core.experimental.schemahistory.ResolvedSchemaHistoryItem;
import org.flywaydb.core.internal.util.Pair;

public class CoreMigrationStateCalculator implements ExperimentalMigrationStateCalculator {
    public MigrationState calculateState(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations,
        final Configuration configuration) {
        if (migration.getLeft() == null) {
            return calculateNoSHTStates(migration, sortedMigrations, configuration);
        }

        return calculateSHTStates(migration, sortedMigrations);
    }

    private static MigrationState calculateNoSHTStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations,
        final Configuration configuration) {
        Optional<MigrationVersion> baselineVersion = sortedMigrations.stream()
            .filter(x -> x.getLeft() != null)
            .filter(x -> x.getLeft().getType().isBaseline())
            .map(x -> x.getLeft().getVersion())
            .findFirst();
        final boolean baselinedSchema = baselineVersion.isPresent();
        if (baselineVersion.isEmpty()) {
            baselineVersion = sortedMigrations.stream()
                .filter(x -> x.getRight() != null)
                .filter(x -> x.getRight().migrationType().isBaseline())
                .map(x -> x.getRight().version())
                .max(MigrationVersion::compareTo);
        }

        if (baselineVersion.isEmpty()
            || migration.getRight().isRepeatable()
            || migration.getRight().version().isNewerThan(baselineVersion.get())) {
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
                final MigrationVersion highestSHTVersion = highestSHTVersion(sortedMigrations);
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

    private static MigrationState calculateSHTStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
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
            final MigrationVersion maxLocalVersion = highestLocalVersion(sortedMigrations);
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

    private static MigrationVersion highestSHTVersion(final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
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

    private static MigrationState calculateLookAheadStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        if (!migration.getLeft().getType().isUndo() && hasFutureUndo(migration, sortedMigrations)) {
            return MigrationState.UNDONE;
        }

        final boolean futureDelete = sortedMigrations.stream()
            .filter(x -> x.getLeft() != null)
            .filter(x -> x.getLeft().getType() == CoreMigrationType.DELETE)
            .filter(x -> x.getLeft().isRepeatable() == migration.getLeft().isRepeatable())
            .anyMatch(x -> x.getLeft().isRepeatable()
                ? x.getLeft()
                .getDescription()
                .equals(migration.getLeft().getDescription())
                : x.getLeft().getVersion().equals(migration.getLeft().getVersion()));
        if (futureDelete && migration.getLeft().getType() != CoreMigrationType.DELETE) {
            return MigrationState.DELETED;
        }

        if (migration.getLeft().isVersioned() && !migration.getLeft().getType().isUndo()) {
            final boolean outOfOrder = sortedMigrations.stream()
                .filter(x -> x.getLeft() != null)
                .filter(x -> !x.getLeft().getType().isUndo())
                .filter(x -> x.getLeft().isVersioned())
                .filter(x -> x.getLeft().getVersion().isNewerThan(migration.getLeft().getVersion()))
                .anyMatch(x -> x.getLeft().getInstalledRank() < migration.getLeft().getInstalledRank());
            if (outOfOrder) {
                return MigrationState.OUT_OF_ORDER;
            }
        }
        return null;
    }

    private static boolean hasFutureUndo(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        return sortedMigrations.stream()
            .filter(x -> x.getLeft() != null)
            .filter(x -> x.getLeft().getType().isUndo())
            .filter(x -> x.getLeft().getInstalledRank() > migration.getLeft().getInstalledRank())
            .anyMatch(x -> x.getLeft().getVersion().equals(migration.getLeft().getVersion()));
    }

    private static MigrationState calculateMissingStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        final MigrationVersion latestLocalVersion = sortedMigrations.stream()
            .filter(x -> x.getRight() != null)
            .filter(x -> x.getRight().isVersioned())
            .map(x -> x.getRight().version())
            .sorted()
            .findFirst()
            .orElse(MigrationVersion.EMPTY);

        if (migration.getLeft().getVersion().isNewerThan(latestLocalVersion)) {
            return MigrationState.FUTURE_SUCCESS;
        }

        if (latestLocalVersion.isNewerThan(migration.getLeft().getVersion())) {
            return MigrationState.MISSING_SUCCESS;
        }
        return null;
    }

    private static MigrationState calculateRepeatableStates(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        final Collection<? extends Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations) {
        final boolean superseded = sortedMigrations.stream()
            .filter(x -> x.getLeft() != null)
            .filter(x -> x.getLeft().isSuccess())
            .filter(x -> x.getLeft().isRepeatable())
            .filter(x -> x.getLeft().getDescription().equals(migration.getLeft().getDescription()))
            .anyMatch(x -> x.getLeft().getInstalledRank() > migration.getLeft().getInstalledRank());
        if (superseded) {
            return MigrationState.SUPERSEDED;
        }

        final boolean outdated = sortedMigrations.stream()
            .filter(x -> x.getLeft() == null)
            .filter(x -> x.getRight().isRepeatable())
            .anyMatch(x -> x.getRight().description().equals(migration.getLeft().getDescription()));

        if (outdated) {
            return MigrationState.OUTDATED;
        }

        if (migration.getRight() == null) {
            return MigrationState.MISSING_SUCCESS;
        }
        return null;
    }
}
