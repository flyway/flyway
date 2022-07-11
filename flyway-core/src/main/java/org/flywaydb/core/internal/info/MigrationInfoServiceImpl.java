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
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;

import java.util.*;

public class MigrationInfoServiceImpl implements MigrationInfoService, OperationResult {
    private final CompositeMigrationResolver migrationResolver;
    private final SchemaHistory schemaHistory;
    private final Database database;
    private final Configuration configuration;
    private final MigrationVersion target;
    private final boolean outOfOrder;
    private final ValidatePattern[] ignorePatterns;
    private final MigrationPattern[] cherryPick;
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
     * @param schemaHistory The schema history table for applied migrations.
     * @param configuration The current configuration.
     * @param target The target version up to which to retrieve the info.
     * @param outOfOrder Allows migrations to be run "out of order".
     * @param cherryPick The migrations to consider when migration.
     */
    public MigrationInfoServiceImpl(CompositeMigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database, final Configuration configuration,
                                    MigrationVersion target, boolean outOfOrder, ValidatePattern[] ignorePatterns, MigrationPattern[] cherryPick) {
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.database = database;
        this.schemaHistory = schemaHistory;
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.ignorePatterns = ignorePatterns;
        this.cherryPick = cherryPick;
    }

    /**
     * Refreshes the info about all known migrations from both the classpath and the DB.
     */
    public void refresh() {
        Collection<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(configuration);
        List<AppliedMigration> appliedMigrations = schemaHistory.allAppliedMigrations();

        MigrationInfoContext context = new MigrationInfoContext();
        context.target = target;
        context.outOfOrder = outOfOrder;
        context.ignorePatterns = ignorePatterns;
        context.cherryPick = cherryPick;

        Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> resolvedVersioned = getResolvedVersionedMigrations(resolvedMigrations, context);
        Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap<>(getResolvedRepeatableMigrations(resolvedMigrations));





        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList<>(getAppliedVersionedMigrations(appliedMigrations, context));
        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable = new ArrayList<>(getAppliedRepeatableMigrations(appliedMigrations));

        updateContextFromAppliedVersionedMigrations(appliedVersioned, context);

        if (MigrationVersion.CURRENT == target) {
            context.target = context.lastApplied;
        }

        List<MigrationInfoImpl> migrationInfos1 = new ArrayList<>();

        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            ResolvedMigration resolvedMigration = resolvedVersioned.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType()));
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, av.getLeft(), context, av.getRight().outOfOrder, av.getRight().deleted, av.getRight().undone));
        }

        for (ResolvedMigration prv : getPendingResolvedVersionedMigrations(appliedVersioned, resolvedVersioned, context)) {
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false, false, false));
        }

        if (configuration.isFailOnMissingTarget() &&
                target != null &&
                target != MigrationVersion.CURRENT &&
                target != MigrationVersion.LATEST &&
                target != MigrationVersion.NEXT) {
            validateTarget(target, migrationInfos1);
        }

        context.latestRepeatableRuns = getLatestRepeatableRuns(appliedRepeatable);

        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatable) {
            AppliedMigration appliedRepeatableMigration = av.getLeft();
            ResolvedMigration resolvedMigration = resolvedRepeatable.get(appliedRepeatableMigration.getDescription());
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false, av.getRight().deleted, false));
        }

        for (ResolvedMigration prr : getPendingResolvedRepeatableMigrations(appliedRepeatable, resolvedRepeatable, context)) {
            migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false, false, false));
        }

        Collections.sort(migrationInfos1);
        migrationInfos = migrationInfos1;

        if (context.target == MigrationVersion.NEXT) {
            MigrationInfo[] pendingMigrationInfos = pending();
            if (pendingMigrationInfos.length == 0) {
                context.target = null;
            } else {
                context.target = pendingMigrationInfos[0].getVersion();
            }
        }
    }

    private Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> getResolvedVersionedMigrations(Collection<ResolvedMigration> resolvedMigrations, MigrationInfoContext context) {
        Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> resolvedVersionedMigrations = new TreeMap<>((p1, p2) -> p1.getLeft().compareTo(p2.getLeft()) == 0 ?
                p1.getRight().toString().compareTo(p2.getRight().toString()) :
                p1.getLeft().compareTo(p2.getLeft()));
        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                resolvedVersionedMigrations.put(Pair.of(version, resolvedMigration.getType()), resolvedMigration);
            }
        }
        return resolvedVersionedMigrations;
    }

    private Map<String, ResolvedMigration> getResolvedRepeatableMigrations(Collection<ResolvedMigration> resolvedMigrations) {
        Map<String, ResolvedMigration> resolvedRepeatableMigrations = new TreeMap<>();
        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            if (resolvedMigration.getVersion() == null) {
                resolvedRepeatableMigrations.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }
        return resolvedRepeatableMigrations;
    }

    private List<Pair<AppliedMigration, AppliedMigrationAttributes>> getAppliedVersionedMigrations(List<AppliedMigration> appliedMigrations, MigrationInfoContext context) {
        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersionedMigrations = new ArrayList<>();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            MigrationVersion version = appliedMigration.getVersion();
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
            }





            appliedVersionedMigrations.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
        }
        return appliedVersionedMigrations;
    }

    private List<Pair<AppliedMigration, AppliedMigrationAttributes>> getAppliedRepeatableMigrations(List<AppliedMigration> appliedMigrations) {
        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatableMigrations = new ArrayList<>();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            if (appliedMigration.getVersion() == null) {
                appliedRepeatableMigrations.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
                if (appliedMigration.getType().equals(CoreMigrationType.DELETE) && appliedMigration.isSuccess()) {
                    markRepeatableAsDeleted(appliedMigration.getDescription(), appliedRepeatableMigrations);
                }
            }
        }
        return appliedRepeatableMigrations;
    }





























    private void validateTarget(MigrationVersion target, List<MigrationInfoImpl> migrationInfos) {
        boolean targetFound = false;
        for (MigrationInfoImpl migration : migrationInfos) {
            if (target.compareTo(migration.getVersion()) == 0) {
                targetFound = true;
                break;
            }
        }
        if (!targetFound) {
            throw new FlywayException("No migration with a target version " + target + " could be found. Ensure target is specified correctly and the migration exists.");
        }
    }

    private Set<ResolvedMigration> getPendingResolvedVersionedMigrations(List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersionedMigrations,
                                                                         Map<Pair<MigrationVersion, MigrationType>, ResolvedMigration> resolvedVersionedMigrations, MigrationInfoContext context) {
        Set<ResolvedMigration> pendingResolvedVersionedMigrations = new HashSet<>(resolvedVersionedMigrations.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersionedMigrations) {
            ResolvedMigration resolvedMigration = resolvedVersionedMigrations.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType()));
            if (resolvedMigration != null
                    && !av.getRight().deleted && av.getLeft().getType() != CoreMigrationType.DELETE



            ) {
                pendingResolvedVersionedMigrations.remove(resolvedMigration);
            }
        }
        for (ResolvedMigration resolvedMigration : pendingResolvedVersionedMigrations) {
            if (resolvedMigration.getType().isBaseline() && (context.pendingBaseline == null || resolvedMigration.getVersion().isNewerThan(context.pendingBaseline.getVersion()))) {
                context.pendingBaseline = resolvedMigration.getVersion();
            }
        }
        return pendingResolvedVersionedMigrations;
    }

    private Set<ResolvedMigration> getPendingResolvedRepeatableMigrations(List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatableMigrations,
                                                                          Map<String, ResolvedMigration> resolvedRepeatableMigrations, MigrationInfoContext context) {
        Set<ResolvedMigration> pendingResolvedVRepeatableMigrations = new HashSet<>(resolvedRepeatableMigrations.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatableMigrations) {
            AppliedMigration appliedRepeatableMigration = av.getLeft();
            String desc = appliedRepeatableMigration.getDescription();
            int rank = appliedRepeatableMigration.getInstalledRank();

            ResolvedMigration resolvedMigration = resolvedRepeatableMigrations.get(desc);
            int latestRank = context.latestRepeatableRuns.get(desc);

            if (!av.getRight().deleted && av.getLeft().getType() != CoreMigrationType.DELETE &&
                    resolvedMigration != null && rank == latestRank &&
                    resolvedMigration.checksumMatches(appliedRepeatableMigration.getChecksum())) {
                pendingResolvedVRepeatableMigrations.remove(resolvedMigration);
            }
        }
        return pendingResolvedVRepeatableMigrations;
    }

    private Map<String, Integer> getLatestRepeatableRuns(List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatableMigrations) {
        Map<String, Integer> latestRepeatableRuns = new HashMap<>();
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatableMigrations) {
            if (av.getRight().deleted && av.getLeft().getType() == CoreMigrationType.DELETE) {
                continue;
            }

            AppliedMigration appliedRepeatableMigration = av.getLeft();
            String desc = appliedRepeatableMigration.getDescription();
            int rank = appliedRepeatableMigration.getInstalledRank();

            if (!latestRepeatableRuns.containsKey(desc) || (rank > latestRepeatableRuns.get(desc))) {
                latestRepeatableRuns.put(desc, rank);
            }
        }
        return latestRepeatableRuns;
    }

    private void updateContextFromAppliedVersionedMigrations(List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersionedMigrations, MigrationInfoContext context) {
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersionedMigrations) {
            AppliedMigration appliedMigration = av.getLeft();
            MigrationVersion version = appliedMigration.getVersion();
            if (version.compareTo(context.lastApplied) > 0) {
                if (av.getLeft().getType() != CoreMigrationType.DELETE && !av.getRight().deleted



                ) {
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
     * @param description The description to match
     * @param appliedRepeatable The discovered applied migrations
     */
    private void markRepeatableAsDeleted(String description, List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable) {
        for (int i = appliedRepeatable.size() - 1; i >= 0; i--) {
            Pair<AppliedMigration, AppliedMigrationAttributes> ar = appliedRepeatable.get(i);
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
     * @param version The version.
     * @param appliedVersioned The applied migrations.
     */
    private void markAsDeleted(MigrationVersion version, List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned) {
        for (int i = appliedVersioned.size() - 1; i >= 0; i--) {
            Pair<AppliedMigration, AppliedMigrationAttributes> av = appliedVersioned.get(i);
            if (!av.getLeft().getType().isSynthetic() && version.equals(av.getLeft().getVersion())) {
                if (av.getRight().deleted) {
                    throw new FlywayException("Corrupted schema history: multiple delete entries for version " + version,
                                              ErrorCode.DUPLICATE_DELETED_MIGRATION);
                } else {
                    av.getRight().deleted = true;
                    return;
                }
            }
        }
    }

























    @Override
    public MigrationInfo[] all() {
        return migrationInfos.toArray(new MigrationInfo[0]);
    }













    @Override
    public MigrationInfo current() {
        MigrationInfo current = null;
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()
                    && !MigrationState.DELETED.equals(migrationInfo.getState())
                    && !migrationInfo.getType().equals(CoreMigrationType.DELETE)




                    && migrationInfo.getVersion() != null
                    && (current == null || migrationInfo.getVersion().compareTo(current.getVersion()) > 0)) {
                current = migrationInfo;
            }
        }
        if (current != null) {
            return current;
        }

        // If no versioned migration has been applied so far, fall back to the latest repeatable one
        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            MigrationInfoImpl migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isApplied()
                    && !MigrationState.DELETED.equals(migrationInfo.getState())
                    && !migrationInfo.getType().equals(CoreMigrationType.DELETE)




            ) {
                return migrationInfo;
            }
        }

        return null;
    }

    @Override
    public MigrationInfoImpl[] pending() {
        List<MigrationInfoImpl> pendingMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (MigrationState.PENDING == migrationInfo.getState()) {
                pendingMigrations.add(migrationInfo);
            }
        }
        return pendingMigrations.toArray(new MigrationInfoImpl[0]);
    }

    @Override
    public MigrationInfoImpl[] applied() {
        List<MigrationInfoImpl> appliedMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()) {
                appliedMigrations.add(migrationInfo);
            }
        }
        return appliedMigrations.toArray(new MigrationInfoImpl[0]);
    }

    /**
     * @return The resolved migrations. An empty array if none.
     */
    public MigrationInfo[] resolved() {
        List<MigrationInfo> resolvedMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isResolved()) {
                resolvedMigrations.add(migrationInfo);
            }
        }
        return resolvedMigrations.toArray(new MigrationInfo[0]);
    }

    /**
     * @return The failed migrations. An empty array if none.
     */
    public MigrationInfoImpl[] failed() {
        List<MigrationInfoImpl> failedMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isFailed()) {
                failedMigrations.add(migrationInfo);
            }
        }
        return failedMigrations.toArray(new MigrationInfoImpl[0]);
    }

    /**
     * @return The future migrations. An empty array if none.
     */
    public MigrationInfo[] future() {
        List<MigrationInfo> futureMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS)
                    || (migrationInfo.getState() == MigrationState.FUTURE_FAILED))




            ) {
                futureMigrations.add(migrationInfo);
            }
        }
        return futureMigrations.toArray(new MigrationInfo[0]);
    }

    /**
     * @return The out-of-order migrations. An empty array if none.
     */
    public MigrationInfo[] outOfOrder() {
        List<MigrationInfo> outOfOrderMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState() == MigrationState.OUT_OF_ORDER) {
                outOfOrderMigrations.add(migrationInfo);
            }
        }
        return outOfOrderMigrations.toArray(new MigrationInfo[0]);
    }

















    /**
     * @return The list of migrations that failed validation, which is empty if everything is fine.
     */
    public List<ValidateOutput> validate() {
        List<ValidateOutput> invalidMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            ErrorDetails validateError = migrationInfo.validate();
            if (validateError != null) {
                invalidMigrations.add(CommandResultFactory.createValidateOutput(migrationInfo, validateError));
            }
        }
        return invalidMigrations;
    }

    public void setAllSchemasEmpty(Schema[] schemas) {
        allSchemasEmpty = Arrays.stream(schemas).filter(Schema::exists).allMatch(Schema::empty);
    }

    @Override
    public InfoResult getInfoResult() {
        return getInfoResult(this.all());
    }

    public InfoResult getInfoResult(MigrationInfo[] infos) {
        return CommandResultFactory.createInfoResult(this.configuration, this.database, infos, this.current(), this.allSchemasEmpty);
    }







}