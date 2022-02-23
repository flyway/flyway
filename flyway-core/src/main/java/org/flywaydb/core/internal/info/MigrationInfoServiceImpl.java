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
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import org.flywaydb.core.api.resolver.UnresolvedMigration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;

import java.util.*;

public class MigrationInfoServiceImpl implements MigrationInfoService, OperationResult {
    private final MigrationResolver migrationResolver;
    private final Configuration configuration;
    private final Database database;
    private final Context context;
    private final SchemaHistory schemaHistory;
    private final MigrationVersion target;
    private final MigrationPattern[] cherryPick;
    private final boolean outOfOrder;
    private final boolean pending;
    private final boolean missing;
    private final boolean ignored;
    private final boolean future;
    /**
     * The migrations infos calculated at the last refresh.
     */
    private List<MigrationInfoImpl> migrationInfos;

    /**
     * The migrations that were detected but failed to resolve at the last refresh.
     */
    private Collection<UnresolvedMigration> unresolvedMigrations;

    /**
     * Whether all of the specified schemas are empty or not.
     */
    private Boolean allSchemasEmpty;

    /**
     * @param migrationResolver The migration resolver for available migrations.
     * @param schemaHistory The schema history table for applied migrations.
     * @param configuration The current configuration.
     * @param target The target version up to which to retrieve the info.
     * @param outOfOrder Allows migrations to be run "out of order".
     * @param cherryPick The migrations to consider when migration.
     * @param pending Whether pending migrations are allowed.
     * @param missing Whether missing migrations are allowed.
     * @param ignored Whether ignored migrations are allowed.
     * @param future Whether future migrations are allowed.
     */
    public MigrationInfoServiceImpl(MigrationResolver migrationResolver,
                                    SchemaHistory schemaHistory, Database database, final Configuration configuration,
                                    MigrationVersion target, boolean outOfOrder, MigrationPattern[] cherryPick,
                                    boolean pending, boolean missing, boolean ignored, boolean future) {
        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.configuration = configuration;
        this.context = () -> configuration;
        this.database = database;
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.cherryPick = cherryPick;
        this.pending = pending;
        this.missing = missing;
        this.ignored = ignored || cherryPick != null;
        this.future = future;
    }

    /**
     * Refreshes the info about all known migrations from both the classpath and the DB.
     */
    public void refresh() {
        Pair<List<ResolvedMigration>, Collection<UnresolvedMigration>> migrations = migrationResolver.attemptResolveMigrations(context);
        List<ResolvedMigration> resolvedMigrations = migrations.getLeft();
        unresolvedMigrations = migrations.getRight();

        List<AppliedMigration> appliedMigrations = schemaHistory.allAppliedMigrations();

        MigrationInfoContext context = new MigrationInfoContext();
        context.outOfOrder = outOfOrder;
        context.pending = pending;
        context.missing = missing;
        context.ignored = ignored;
        context.future = future;
        context.ignorePatterns = configuration.getIgnoreMigrationPatterns();
        context.target = target;
        context.cherryPick = cherryPick;

        Map<Pair<MigrationVersion, Boolean>, ResolvedMigration> resolvedVersioned = new TreeMap<>();
        Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap<>();
        ResolvedMigration pendingBaselineMigration = null;
        AppliedMigration appliedBaselineMigration = null;

        // Separate resolved migrations into versioned and repeatable
        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                if (resolvedMigration.getType().isBaselineMigration() && version.compareTo(context.latestBaselineMigration) > 0) {




                } else {
                    //noinspection RedundantConditionalExpression
                    resolvedVersioned.put(Pair.of(version,



                                                          false), resolvedMigration);
                }
            } else {
                resolvedRepeatable.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }




























        // Split applied into version and repeatable, and update state from synthetic migrations
        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList<>();
        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable = new ArrayList<>();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            MigrationVersion version = appliedMigration.getVersion();
            if (version == null) {
                appliedRepeatable.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));

                if (appliedMigration.getType().equals(MigrationType.DELETE) && appliedMigration.isSuccess()) {
                    markRepeatableAsDeleted(appliedMigration.getDescription(), appliedRepeatable);
                }
                continue;
            }
            if (appliedMigration.getType() == MigrationType.SCHEMA) {
                context.schema = version;
            }
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                context.baseline = version;
            }
            if (appliedMigration.getType().equals(MigrationType.DELETE) && appliedMigration.isSuccess()) {
                markAsDeleted(version, appliedVersioned);
            }





            appliedVersioned.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
        }

        // Update last applied and out of order states
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            AppliedMigration appliedMigration = av.getLeft();
            MigrationVersion version = appliedMigration.getVersion();
            if (version != null) {





                if (version.compareTo(context.lastApplied) > 0) {
                    if (av.getLeft().getType() != MigrationType.DELETE && !av.getRight().deleted



                    ) {
                        context.lastApplied = version;
                    }
                } else {
                    av.getRight().outOfOrder = true;
                }
            }
        }











        // Set target
        if (MigrationVersion.CURRENT == target) {
            context.target = context.lastApplied;
        }

        // Identify pending versioned migrations and build output migration info list
        List<MigrationInfoImpl> migrationInfos1 = new ArrayList<>();
        Set<ResolvedMigration> pendingResolvedVersioned = new HashSet<>(resolvedVersioned.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            ResolvedMigration resolvedMigration = resolvedVersioned.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType().isUndo()));

            // Remove pending migrations
            if (resolvedMigration != null
                    && !av.getRight().deleted && av.getLeft().getType() != MigrationType.DELETE



            ) {
                pendingResolvedVersioned.remove(resolvedMigration);
            }

            // Build final migration info
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, av.getLeft(), context, av.getRight().outOfOrder, av.getRight().deleted, av.getRight().undone));
        }

        // Add all pending migrations to output list
        for (ResolvedMigration prv : pendingResolvedVersioned) {
            if (prv.getVersion().compareTo(context.latestBaselineMigration) <= 0) {
                continue;
            }
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false, false, false));
        }

        if (pendingBaselineMigration != null) {
            migrationInfos1.add(new MigrationInfoImpl(pendingBaselineMigration, null, context, false, false, false));
        }

        if (configuration.isFailOnMissingTarget() &&
                target != null &&
                target != MigrationVersion.CURRENT &&
                target != MigrationVersion.LATEST &&
                target != MigrationVersion.NEXT) {
            boolean targetFound = false;

            for (MigrationInfoImpl migration : migrationInfos1) {
                if (target.compareTo(migration.getVersion()) == 0) {
                    targetFound = true;
                    break;
                }
            }

            if (!targetFound) {
                throw new FlywayException("No migration with a target version " + target + " could be found. Ensure target is specified correctly and the migration exists.");
            }
        }

        // Setup the latest repeatable run ranks
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatable) {
            if (av.getRight().deleted && av.getLeft().getType() == MigrationType.DELETE) {
                continue;
            }
            AppliedMigration appliedRepeatableMigration = av.getLeft();

            String desc = appliedRepeatableMigration.getDescription();
            int rank = appliedRepeatableMigration.getInstalledRank();
            Map<String, Integer> latestRepeatableRuns = context.latestRepeatableRuns;

            if (!latestRepeatableRuns.containsKey(desc) || (rank > latestRepeatableRuns.get(desc))) {
                latestRepeatableRuns.put(desc, rank);
            }
        }

        // Using latest repeatable runs, discover pending repeatables and build output list
        Set<ResolvedMigration> pendingResolvedRepeatable = new HashSet<>(resolvedRepeatable.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatable) {
            AppliedMigration appliedRepeatableMigration = av.getLeft();

            String desc = appliedRepeatableMigration.getDescription();
            int rank = appliedRepeatableMigration.getInstalledRank();

            ResolvedMigration resolvedMigration = resolvedRepeatable.get(desc);
            int latestRank = context.latestRepeatableRuns.get(desc);

            // If latest run is the same rank, its not pending
            if (!av.getRight().deleted && av.getLeft().getType() != MigrationType.DELETE
                    && resolvedMigration != null && rank == latestRank && resolvedMigration.checksumMatches(appliedRepeatableMigration.getChecksum())) {
                pendingResolvedRepeatable.remove(resolvedMigration);
            }

            // Add to output list
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false, av.getRight().deleted, false));
        }

        // Add pending repeatables to output list
        for (ResolvedMigration prr : pendingResolvedRepeatable) {
            migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false, false, false));
        }

        // Set output
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
                    && !migrationInfo.getType().equals(MigrationType.DELETE)




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
                    && !migrationInfo.getType().equals(MigrationType.DELETE)




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

    public Collection<UnresolvedMigration> unresolved() {
        return this.unresolvedMigrations;
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
     * @return The out of order migrations. An empty array if none.
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
        return CommandResultFactory.createInfoResult(this.context.getConfiguration(), this.database, infos, this.current(), this.allSchemasEmpty);
    }







}