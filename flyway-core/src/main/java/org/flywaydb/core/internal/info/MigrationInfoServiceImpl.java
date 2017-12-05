/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Default implementation of MigrationInfoService.
 */
public class MigrationInfoServiceImpl implements MigrationInfoService {
    /**
     * The migration resolver for available migrations.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The schema history table for applied migrations.
     */
    private final SchemaHistory schemaHistory;

    /**
     * The target version up to which to retrieve the info.
     */
    private MigrationVersion target;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private boolean outOfOrder;

    /**
     * Whether pending migrations are allowed.
     */
    private final boolean pending;

    /**
     * Whether missing migrations are allowed.
     */
    private final boolean missing;

    /**
     * Whether future migrations are allowed.
     */
    private final boolean future;

    /**
     * The migrations infos calculated at the last refresh.
     */
    private List<MigrationInfoImpl> migrationInfos;

    /**
     * Creates a new MigrationInfoServiceImpl.
     *
     * @param migrationResolver The migration resolver for available migrations.
     * @param schemaHistory     The schema history table for applied migrations.
     * @param target            The target version up to which to retrieve the info.
     * @param outOfOrder        Allows migrations to be run "out of order".
     * @param pending           Whether pending migrations are allowed.
     * @param missing           Whether missing migrations are allowed.
     * @param future            Whether future migrations are allowed.
     */
    public MigrationInfoServiceImpl(MigrationResolver migrationResolver,
                                    SchemaHistory schemaHistory,
                                    MigrationVersion target, boolean outOfOrder, boolean pending, boolean missing, boolean future) {
        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.pending = pending;
        this.missing = missing;
        this.future = future;
    }

    /**
     * Refreshes the info about all known migrations from both the classpath and the DB.
     */
    public void refresh() {
        Collection<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations();
        List<AppliedMigration> appliedMigrations = schemaHistory.allAppliedMigrations();

        MigrationInfoContext context = new MigrationInfoContext();
        context.outOfOrder = outOfOrder;
        context.pending = pending;
        context.missing = missing;
        context.future = future;
        context.target = target;

        Map<Pair<MigrationVersion, Boolean>, ResolvedMigration> resolvedVersioned =
                new TreeMap<Pair<MigrationVersion, Boolean>, ResolvedMigration>();
        Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap<String, ResolvedMigration>();

        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                //noinspection RedundantConditionalExpression
                resolvedVersioned.put(Pair.of(version,
                        // [pro]
                        resolvedMigration.getType().isUndo() ? true :
                                // [/pro]
                                false), resolvedMigration);
            } else {
                resolvedRepeatable.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }

        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList<Pair<AppliedMigration, AppliedMigrationAttributes>>();
        List<AppliedMigration> appliedRepeatable = new ArrayList<AppliedMigration>();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            MigrationVersion version = appliedMigration.getVersion();
            if (version == null) {
                appliedRepeatable.add(appliedMigration);
                continue;
            }
            if (appliedMigration.getType() == MigrationType.SCHEMA) {
                context.schema = version;
            }
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                context.baseline = version;
            }
            // [pro]
            if (appliedMigration.getType().isUndo() && appliedMigration.isSuccess()) {
                markAsUndone(appliedMigration.getVersion(), appliedVersioned);
            }
            // [/pro]
            appliedVersioned.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
        }

        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            MigrationVersion version = av.getLeft().getVersion();
            if (version != null) {
                if (version.compareTo(context.lastApplied) > 0) {
                    // [pro]
                    if (!av.getLeft().getType().isUndo() && !av.getRight().undone) {
                        // [/pro]
                        context.lastApplied = version;
                        // [pro]
                    }
                    // [/pro]
                } else {
                    av.getRight().outOfOrder = true;
                }
            }
        }

        if (MigrationVersion.CURRENT == target) {
            context.target = context.lastApplied;
        }

        Set<Pair<MigrationVersion, Boolean>> allVersions = new HashSet<Pair<MigrationVersion, Boolean>>(resolvedVersioned.keySet());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            // [pro]
            if (!av.getLeft().getType().isUndo() && !av.getRight().undone) {
                // [/pro]
                //noinspection RedundantConditionalExpression
                allVersions.add(Pair.of(av.getLeft().getVersion(),
                        // [pro]
                        av.getLeft().getType().isUndo() ? true :
                                // [/pro]
                                false
                ));
                // [pro]
            }
            // [/pro]
        }

        List<MigrationInfoImpl> migrationInfos1 = new ArrayList<MigrationInfoImpl>();
        Set<ResolvedMigration> pendingResolvedVersioned = new HashSet<ResolvedMigration>(resolvedVersioned.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            ResolvedMigration resolvedMigration = resolvedVersioned.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType().isUndo()));
            if (resolvedMigration != null
                    // [pro]
                    && !av.getRight().undone
                    // [/pro]
                    && av.getLeft().isSuccess()) {
                pendingResolvedVersioned.remove(resolvedMigration);
            }
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, av.getLeft(), context, av.getRight().outOfOrder
                    // [pro]
                    , av.getRight().undone
                    // [/pro]
            ));
        }

        for (ResolvedMigration prv : pendingResolvedVersioned) {
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false
                    // [pro]
                    , false
                    // [/pro]
            ));
        }


        for (AppliedMigration appliedRepeatableMigration : appliedRepeatable) {
            if (!context.latestRepeatableRuns.containsKey(appliedRepeatableMigration.getDescription())
                    || (appliedRepeatableMigration.getInstalledRank() > context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription()))) {
                context.latestRepeatableRuns.put(appliedRepeatableMigration.getDescription(), appliedRepeatableMigration.getInstalledRank());
            }
        }

        Set<ResolvedMigration> pendingResolvedRepeatable = new HashSet<ResolvedMigration>(resolvedRepeatable.values());
        for (AppliedMigration appliedRepeatableMigration : appliedRepeatable) {
            ResolvedMigration resolvedMigration = resolvedRepeatable.get(appliedRepeatableMigration.getDescription());
            int latestRank = context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription());
            if (resolvedMigration != null && appliedRepeatableMigration.getInstalledRank() == latestRank && ObjectUtils.nullSafeEquals(appliedRepeatableMigration.getChecksum(), resolvedMigration.getChecksum())) {
                pendingResolvedRepeatable.remove(resolvedMigration);
            }
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false
                    // [pro]
                    , false
                    // [/pro]
            ));
        }

        for (ResolvedMigration prr : pendingResolvedRepeatable) {
            migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false
                    // [pro]
                    , false
                    // [/pro]
            ));
        }

        Collections.sort(migrationInfos1);
        migrationInfos = migrationInfos1;
    }

    // [pro]

    /**
     * Marks the latest applied migration with this version as undone.
     *
     * @param version          The version.
     * @param appliedVersioned The applied migrations.
     */
    private void markAsUndone(MigrationVersion version, List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned) {
        for (int i = appliedVersioned.size() - 1; i >= 0; i--) {
            Pair<AppliedMigration, AppliedMigrationAttributes> av = appliedVersioned.get(i);
            if (!av.getLeft().getType().isUndo() && version.equals(av.getLeft().getVersion())) {
                if (av.getRight().undone) {
                    throw new FlywayException("Corrupted schema history: multiple undo entries for version " + version);
                } else {
                    av.getRight().undone = true;
                    return;
                }
            }
        }
    }
    // [/pro]

    public MigrationInfo[] all() {
        return migrationInfos.toArray(new MigrationInfoImpl[migrationInfos.size()]);
    }

    public MigrationInfo current() {
        MigrationInfo current = null;
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()
                    // [pro]
                    && !MigrationState.UNDONE.equals(migrationInfo.getState())
                    && !migrationInfo.getType().isUndo()
                    // [/pro]
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
                    // [pro]
                    && !MigrationState.UNDONE.equals(migrationInfo.getState())
                    && !migrationInfo.getType().isUndo()
                // [/pro]
                    ) {
                return migrationInfo;
            }
        }

        return null;
    }

    public MigrationInfoImpl[] pending() {
        List<MigrationInfoImpl> pendingMigrations = new ArrayList<MigrationInfoImpl>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (MigrationState.PENDING == migrationInfo.getState()) {
                pendingMigrations.add(migrationInfo);
            }
        }

        return pendingMigrations.toArray(new MigrationInfoImpl[pendingMigrations.size()]);
    }

    public MigrationInfoImpl[] applied() {
        List<MigrationInfoImpl> appliedMigrations = new ArrayList<MigrationInfoImpl>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()) {
                appliedMigrations.add(migrationInfo);
            }
        }

        return appliedMigrations.toArray(new MigrationInfoImpl[appliedMigrations.size()]);
    }

    /**
     * Retrieves the full set of infos about the migrations resolved on the classpath.
     *
     * @return The resolved migrations. An empty array if none.
     */
    public MigrationInfo[] resolved() {
        List<MigrationInfo> resolvedMigrations = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isResolved()) {
                resolvedMigrations.add(migrationInfo);
            }
        }

        return resolvedMigrations.toArray(new MigrationInfo[resolvedMigrations.size()]);
    }

    /**
     * Retrieves the full set of infos about the migrations that failed.
     *
     * @return The failed migrations. An empty array if none.
     */
    public MigrationInfo[] failed() {
        List<MigrationInfo> failedMigrations = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isFailed()) {
                failedMigrations.add(migrationInfo);
            }
        }

        return failedMigrations.toArray(new MigrationInfo[failedMigrations.size()]);
    }

    /**
     * Retrieves the full set of infos about future migrations applied to the DB.
     *
     * @return The future migrations. An empty array if none.
     */
    public MigrationInfo[] future() {
        List<MigrationInfo> futureMigrations = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if ((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS)
                    || (migrationInfo.getState() == MigrationState.FUTURE_FAILED)) {
                futureMigrations.add(migrationInfo);
            }
        }

        return futureMigrations.toArray(new MigrationInfo[futureMigrations.size()]);
    }

    /**
     * Retrieves the full set of infos about out of order migrations applied to the DB.
     *
     * @return The out of order migrations. An empty array if none.
     */
    public MigrationInfo[] outOfOrder() {
        List<MigrationInfo> outOfOrderMigrations = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState() == MigrationState.OUT_OF_ORDER) {
                outOfOrderMigrations.add(migrationInfo);
            }
        }

        return outOfOrderMigrations.toArray(new MigrationInfo[outOfOrderMigrations.size()]);
    }

    // [pro]

    /**
     * Retrieves the full set of infos about undo migrations.
     *
     * @return The undo migrations. An empty array if none.
     */
    public MigrationInfoImpl[] undo() {
        List<MigrationInfoImpl> result = new ArrayList<MigrationInfoImpl>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getType().isUndo()) {
                result.add(migrationInfo);
            }
        }

        return result.toArray(new MigrationInfoImpl[result.size()]);
    }
    // [/pro]

    /**
     * Validate all migrations for consistency.
     *
     * @return The error message, or {@code null} if everything is fine.
     */
    public String validate() {
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            String message = migrationInfo.validate();
            if (message != null) {
                return message;
            }
        }
        return null;
    }
}
