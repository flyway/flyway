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
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.MigrationFilter;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;

import java.util.*;

public class MigrationInfoServiceImpl implements MigrationInfoService {
    private final CompositeMigrationResolver migrationResolver;
    private final SchemaHistory schemaHistory;
    private final Database database;
    private final Configuration configuration;
    private final MigrationVersion target;
    private final boolean outOfOrder;
    private final ValidatePattern[] ignorePatterns;
    /**
     * The migrations infos calculated at the last refresh.
     */
    private List<MigrationInfoImpl> migrationInfos;
    /**
     * Whether all the specified schemas are empty or not.
     */
    private Boolean allSchemasEmpty;

    /**
     * @param migrationResolver The migration resolver for available migrations.
     * @param schemaHistory     The schema history table for applied migrations.
     * @param configuration     The current configuration.
     * @param target            The target version up to which to retrieve the info.
     * @param outOfOrder        Allows migrations to be run "out of order".
     */
    public MigrationInfoServiceImpl(final CompositeMigrationResolver migrationResolver,
        final SchemaHistory schemaHistory,
        final Database database,
        final Configuration configuration,
        final MigrationVersion target,
        final boolean outOfOrder,
        final ValidatePattern[] ignorePatterns) {
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.database = database;
        this.schemaHistory = schemaHistory;
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.ignorePatterns = ignorePatterns;
    }

    /**
     * Refreshes the info about all known migrations from both the classpath and the DB.
     */
    public void refresh() {
        final Collection<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(configuration);
        final List<AppliedMigration> appliedMigrations = schemaHistory.allAppliedMigrations();

        final MigrationInfoContext context = new MigrationInfoContext(configuration);
        context.target = target;
        context.outOfOrder = outOfOrder;
        context.ignorePatterns = ignorePatterns;

        final Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> resolvedVersioned = getResolvedVersionedMigrations(
            resolvedMigrations,
            context);
        final Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap<>(getResolvedRepeatableMigrations(
            resolvedMigrations));

        context.cherryPickSupport.validatePatterns(context.cherryPick,
            resolvedMigrations,
            appliedMigrations,
            configuration);

        final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList<>(
            getAppliedVersionedMigrations(appliedMigrations, context));
        final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable = new ArrayList<>(
            getAppliedRepeatableMigrations(appliedMigrations));

        updateContextFromAppliedVersionedMigrations(appliedVersioned, context);

        if (MigrationVersion.CURRENT == target) {
            context.target = context.lastApplied;
        }

        final List<MigrationInfoImpl> migrationInfos1 = new ArrayList<>();

        for (final Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            final ResolvedMigration resolvedMigration = resolvedVersioned.get(Pair.of(av.getLeft().getVersion(),
                av.getLeft().getType()));
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration,
                av.getLeft(),
                context,
                av.getRight().outOfOrder,
                av.getRight().deleted,
                av.getRight().undone));
        }

        for (final ResolvedMigration prv : getPendingResolvedVersionedMigrations(appliedVersioned,
            resolvedVersioned,
            context)) {
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false, false, false));
        }

        if (configuration.isFailOnMissingTarget()
            && target != null
            && target != MigrationVersion.CURRENT
            && target != MigrationVersion.LATEST
            && target != MigrationVersion.NEXT) {
            validateTarget(target, migrationInfos1);
        }

        context.latestRepeatableRuns = getLatestRepeatableRuns(appliedRepeatable);

        for (final Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatable) {
            final AppliedMigration appliedRepeatableMigration = av.getLeft();
            final ResolvedMigration resolvedMigration = resolvedRepeatable.get(appliedRepeatableMigration.getDescription());
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration,
                appliedRepeatableMigration,
                context,
                false,
                av.getRight().deleted,
                false));
        }

        for (final ResolvedMigration prr : getPendingResolvedRepeatableMigrations(appliedRepeatable,
            resolvedRepeatable,
            context)) {
            migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false, false, false));
        }

        Collections.sort(migrationInfos1);
        migrationInfos = migrationInfos1;

        if (context.target == MigrationVersion.NEXT) {
            final MigrationInfo[] pendingMigrationInfos = pending();
            if (pendingMigrationInfos.length == 0) {
                context.target = null;
            } else {
                context.target = pendingMigrationInfos[0].getVersion();
            }
        }
    }

    private Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> getResolvedVersionedMigrations(final Collection<ResolvedMigration> resolvedMigrations,
        final MigrationInfoContext context) {
        final Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> resolvedVersionedMigrations = new TreeMap<>(
            (p1, p2) -> p1.getLeft().compareTo(p2.getLeft()) == 0 ? p1.getRight()
                .toString()
                .compareTo(p2.getRight().toString()) : p1.getLeft().compareTo(p2.getLeft()));
        for (final ResolvedMigration resolvedMigration : resolvedMigrations) {
            final MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                resolvedVersionedMigrations.put(Pair.of(version, resolvedMigration.getType()), resolvedMigration);
            }
        }
        return resolvedVersionedMigrations;
    }

    private Map<String, ResolvedMigration> getResolvedRepeatableMigrations(final Collection<ResolvedMigration> resolvedMigrations) {
        final Map<String, ResolvedMigration> resolvedRepeatableMigrations = new TreeMap<>();
        for (final ResolvedMigration resolvedMigration : resolvedMigrations) {
            if (resolvedMigration.getVersion() == null) {
                resolvedRepeatableMigrations.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }
        return resolvedRepeatableMigrations;
    }

    private List<Pair<AppliedMigration, AppliedMigrationAttributes>> getAppliedVersionedMigrations(final List<AppliedMigration> appliedMigrations,
        final MigrationInfoContext context) {
        final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersionedMigrations = new ArrayList<>();
        for (final AppliedMigration appliedMigration : appliedMigrations) {
            appliedMigration.updateAttributes(appliedVersionedMigrations);

            final MigrationVersion version = appliedMigration.getVersion();
            if (version == null) {
                continue;
            }
            if (appliedMigration.getType() == CoreMigrationType.SCHEMA) {
                context.schema = version;
            }
            if (appliedMigration.getType().isBaseline()) {
                if (context.appliedBaseline == null || version.isNewerThan(context.appliedBaseline.getVersion())) {
                    context.appliedBaseline = version;
                }
            }
            if (appliedMigration.getType().equals(CoreMigrationType.DELETE) && appliedMigration.isSuccess()) {
                markAsDeleted(version, appliedVersionedMigrations);
                continue;
            }

            appliedVersionedMigrations.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
        }
        return appliedVersionedMigrations;
    }

    private List<Pair<AppliedMigration, AppliedMigrationAttributes>> getAppliedRepeatableMigrations(final List<AppliedMigration> appliedMigrations) {
        final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatableMigrations = new ArrayList<>();
        for (final AppliedMigration appliedMigration : appliedMigrations) {
            if (appliedMigration.getVersion() == null) {
                appliedRepeatableMigrations.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
                if (appliedMigration.getType().equals(CoreMigrationType.DELETE) && appliedMigration.isSuccess()) {
                    markRepeatableAsDeleted(appliedMigration.getDescription(), appliedRepeatableMigrations);
                }
            }
        }
        return appliedRepeatableMigrations;
    }

    private void validateTarget(final MigrationVersion target, final List<MigrationInfoImpl> migrationInfos) {
        boolean targetFound = false;
        for (final MigrationInfoImpl migration : migrationInfos) {
            if (target.compareTo(migration.getVersion()) == 0) {
                targetFound = true;
                break;
            }
        }
        if (!targetFound) {
            throw new FlywayException("No migration with a target version "
                + target
                + " could be found. Ensure target is specified correctly and the migration exists.");
        }
    }

    private Set<ResolvedMigration> getPendingResolvedVersionedMigrations(final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersionedMigrations,
        final Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> resolvedVersionedMigrations,
        final MigrationInfoContext context) {
        final Set<ResolvedMigration> pendingResolvedVersionedMigrations = new HashSet<>(resolvedVersionedMigrations.values());
        for (final Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersionedMigrations) {
            final ResolvedMigration resolvedMigration = resolvedVersionedMigrations.get(Pair.of(av.getLeft()
                .getVersion(), av.getLeft().getType()));
            if (resolvedMigration != null
                && !av.getRight().deleted
                && av.getLeft().getType() != CoreMigrationType.DELETE
                && !av.getRight().undone) {
                pendingResolvedVersionedMigrations.remove(resolvedMigration);
            }
        }
        for (final ResolvedMigration resolvedMigration : pendingResolvedVersionedMigrations) {
            if (resolvedMigration.getType().isBaseline() && (context.pendingBaseline == null
                || resolvedMigration.getVersion().isNewerThan(context.pendingBaseline.getVersion()))) {
                context.pendingBaseline = resolvedMigration.getVersion();
            }
        }
        return pendingResolvedVersionedMigrations;
    }

    private Set<ResolvedMigration> getPendingResolvedRepeatableMigrations(final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatableMigrations,
        final Map<String, ResolvedMigration> resolvedRepeatableMigrations,
        final MigrationInfoContext context) {
        final Set<ResolvedMigration> pendingResolvedVRepeatableMigrations = new HashSet<>(resolvedRepeatableMigrations.values());
        for (final Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatableMigrations) {
            final AppliedMigration appliedRepeatableMigration = av.getLeft();
            final String desc = appliedRepeatableMigration.getDescription();
            final int rank = appliedRepeatableMigration.getInstalledRank();

            final ResolvedMigration resolvedMigration = resolvedRepeatableMigrations.get(desc);
            final int latestRank = context.latestRepeatableRuns.get(desc);

            if (!av.getRight().deleted
                && av.getLeft().getType() != CoreMigrationType.DELETE
                && resolvedMigration != null
                && rank == latestRank
                && resolvedMigration.checksumMatches(appliedRepeatableMigration.getChecksum())) {
                pendingResolvedVRepeatableMigrations.remove(resolvedMigration);
            }
        }
        return pendingResolvedVRepeatableMigrations;
    }

    private Map<String, Integer> getLatestRepeatableRuns(final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatableMigrations) {
        final Map<String, Integer> latestRepeatableRuns = new HashMap<>();
        for (final Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatableMigrations) {
            if (av.getRight().deleted && av.getLeft().getType() == CoreMigrationType.DELETE) {
                continue;
            }

            final AppliedMigration appliedRepeatableMigration = av.getLeft();
            final String desc = appliedRepeatableMigration.getDescription();
            final int rank = appliedRepeatableMigration.getInstalledRank();

            if (!latestRepeatableRuns.containsKey(desc) || (rank > latestRepeatableRuns.get(desc))) {
                latestRepeatableRuns.put(desc, rank);
            }
        }
        return latestRepeatableRuns;
    }

    private void updateContextFromAppliedVersionedMigrations(final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersionedMigrations,
        final MigrationInfoContext context) {
        for (final Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersionedMigrations) {
            final AppliedMigration appliedMigration = av.getLeft();
            final MigrationVersion version = appliedMigration.getVersion();
            if (version.compareTo(context.lastApplied) > 0) {
                if (av.getLeft().getType() != CoreMigrationType.DELETE && !av.getRight().deleted && av.getLeft()
                    .isVersioned() && !av.getRight().undone) {
                    context.lastApplied = version;
                }
            } else {
                av.getRight().outOfOrder = true;
            }
        }
    }

    /**
     * Marks the latest applied migration with this description as deleted.
     *
     * @param description       The description to match
     * @param appliedRepeatable The discovered applied migrations
     */
    private void markRepeatableAsDeleted(final String description,
        final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable) {
        for (int i = appliedRepeatable.size() - 1; i >= 0; i--) {
            final Pair<AppliedMigration, AppliedMigrationAttributes> ar = appliedRepeatable.get(i);
            if (!ar.getLeft().getType().isSynthetic() && description.equals(ar.getLeft().getDescription())) {
                if (!ar.getRight().deleted) {
                    ar.getRight().deleted = true;
                }
                return;
            }
        }
    }

    /**
     * Marks the latest applied migration with this version as deleted.
     *
     * @param version          The version.
     * @param appliedVersioned The applied migrations.
     */
    private void markAsDeleted(final MigrationVersion version,
        final List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned) {
        for (int i = appliedVersioned.size() - 1; i >= 0; i--) {
            final Pair<AppliedMigration, AppliedMigrationAttributes> av = appliedVersioned.get(i);
            if (!av.getLeft().getType().isSynthetic() && version.equals(av.getLeft().getVersion())) {
                if (av.getRight().deleted) {
                    throw new FlywayException("Corrupted schema history: multiple delete entries for version "
                        + version, CoreErrorCode.DUPLICATE_DELETED_MIGRATION);
                } else {
                    av.getRight().deleted = true;
                    return;
                }
            }
        }
    }

    @Override
    public MigrationInfo[] all() {
        return migrationInfos.toArray(MigrationInfo[]::new);
    }

    public MigrationInfo[] all(final MigrationFilter filter) {
        if (filter == null) {
            return migrationInfos.toArray(MigrationInfo[]::new);
        }

        return migrationInfos.stream()
            .filter(m -> filter.matches(m) || m.getState() == MigrationState.AVAILABLE)
            .toArray(MigrationInfo[]::new);
    }

    @Override
    public MigrationInfo current() {
        MigrationInfo current = null;
        for (final MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()
                && !MigrationState.DELETED.equals(migrationInfo.getState())
                && !migrationInfo.getType().equals(CoreMigrationType.DELETE)
                && !MigrationState.UNDONE.equals(migrationInfo.getState())
                && migrationInfo.isVersioned()
                && (current == null || migrationInfo.getVersion().compareTo(current.getVersion()) > 0)) {
                current = migrationInfo;
            }
        }
        if (current != null) {
            return current;
        }

        // If no versioned migration has been applied so far, fall back to the latest repeatable one
        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            final MigrationInfoImpl migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isApplied()
                && !MigrationState.DELETED.equals(migrationInfo.getState())
                && !migrationInfo.getType().equals(CoreMigrationType.DELETE)
                && !MigrationState.UNDONE.equals(migrationInfo.getState())
                && migrationInfo.getVersion() == null) {
                return migrationInfo;
            }
        }

        return null;
    }

    @Override
    public MigrationInfoImpl[] pending() {
        final List<MigrationInfoImpl> pendingMigrations = new ArrayList<>();
        for (final MigrationInfoImpl migrationInfo : migrationInfos) {
            if (MigrationState.PENDING == migrationInfo.getState()) {
                pendingMigrations.add(migrationInfo);
            }
        }
        return pendingMigrations.toArray(MigrationInfoImpl[]::new);
    }

    @Override
    public MigrationInfoImpl[] applied() {
        final List<MigrationInfoImpl> appliedMigrations = new ArrayList<>();
        for (final MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()) {
                appliedMigrations.add(migrationInfo);
            }
        }
        return appliedMigrations.toArray(MigrationInfoImpl[]::new);
    }

    /**
     * @return The resolved migrations. An empty array if none.
     */
    public MigrationInfo[] resolved() {
        final List<MigrationInfo> resolvedMigrations = new ArrayList<>();
        for (final MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isResolved()) {
                resolvedMigrations.add(migrationInfo);
            }
        }
        return resolvedMigrations.toArray(MigrationInfo[]::new);
    }

    /**
     * @return The failed migrations. An empty array if none.
     */
    public MigrationInfoImpl[] failed() {
        final List<MigrationInfoImpl> failedMigrations = new ArrayList<>();
        for (final MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isFailed()) {
                failedMigrations.add(migrationInfo);
            }
        }
        return failedMigrations.toArray(MigrationInfoImpl[]::new);
    }

    /**
     * @return The future migrations. An empty array if none.
     */
    public MigrationInfo[] future() {
        final List<MigrationInfo> futureMigrations = new ArrayList<>();
        for (final MigrationInfo migrationInfo : migrationInfos) {
            if (((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS) || (migrationInfo.getState()
                == MigrationState.FUTURE_FAILED)) && migrationInfo.isVersioned()) {
                futureMigrations.add(migrationInfo);
            }
        }
        return futureMigrations.toArray(MigrationInfo[]::new);
    }

    /**
     * @return The out-of-order migrations. An empty array if none.
     */
    public MigrationInfo[] outOfOrder() {
        final List<MigrationInfo> outOfOrderMigrations = new ArrayList<>();
        for (final MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState() == MigrationState.OUT_OF_ORDER) {
                outOfOrderMigrations.add(migrationInfo);
            }
        }
        return outOfOrderMigrations.toArray(MigrationInfo[]::new);
    }

    /**
     * @return The undo migrations. An empty array if none.
     */
    public MigrationInfoImpl[] undo() {
        final List<MigrationInfoImpl> result = new ArrayList<>();
        for (final MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getType().isUndo()) {
                result.add(migrationInfo);
            }
        }
        return result.toArray(MigrationInfoImpl[]::new);
    }

    /**
     * @return The list of migrations that failed validation, which is empty if everything is fine.
     */
    public List<ValidateOutput> validate() {
        final List<ValidateOutput> invalidMigrations = new ArrayList<>();
        for (final MigrationInfoImpl migrationInfo : migrationInfos) {
            final ErrorDetails validateError = migrationInfo.validate();
            if (validateError != null) {
                invalidMigrations.add(CommandResultFactory.createValidateOutput(migrationInfo, validateError));
            }
        }
        return invalidMigrations;
    }

    public void setAllSchemasEmpty(final Schema[] schemas) {
        allSchemasEmpty = Arrays.stream(schemas).filter(Schema::exists).allMatch(Schema::empty);
    }

    @Override
    public InfoResult getInfoResult() {
        return getInfoResult(this.all());
    }

    public InfoResult getInfoResult(final MigrationInfo[] infos) {
        return CommandResultFactory.createInfoResult(this.configuration,
            this.database,
            infos,
            this.current(),
            this.allSchemasEmpty);
    }

    @Override
    public InfoResult getInfoResult(final MigrationFilter filter) {
        return getInfoResult(this.all(filter));
    }
}
