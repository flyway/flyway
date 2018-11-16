/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
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

    private final Context context;

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
     * Whether ignored migrations are allowed.
     */
    private final boolean ignored;

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
     * @param configuration     The current configuration.
     * @param target            The target version up to which to retrieve the info.
     * @param outOfOrder        Allows migrations to be run "out of order".
     * @param pending           Whether pending migrations are allowed.
     * @param missing           Whether missing migrations are allowed.
     * @param ignored           Whether ignored migrations are allowed.
     * @param future            Whether future migrations are allowed.
     */
    public MigrationInfoServiceImpl(MigrationResolver migrationResolver,
                                    SchemaHistory schemaHistory, final Configuration configuration,
                                    MigrationVersion target, boolean outOfOrder,
                                    boolean pending, boolean missing, boolean ignored, boolean future) {
        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.context = new Context() {
            @Override
            public Configuration getConfiguration() {
                return configuration;
            }
        };
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.pending = pending;
        this.missing = missing;
        this.ignored = ignored;
        this.future = future;
    }

    /**
     * Refreshes the info about all known migrations from both the classpath and the DB.
     */
    public void refresh() {
        Collection<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(context);
        List<AppliedMigration> appliedMigrations = schemaHistory.allAppliedMigrations();

        MigrationInfoContext context = new MigrationInfoContext();
        context.outOfOrder = outOfOrder;
        context.pending = pending;
        context.missing = missing;
        context.ignored = ignored;
        context.future = future;
        context.target = target;

        Map<Pair<MigrationVersion, Boolean>, ResolvedMigration> resolvedVersioned =
                new TreeMap<>();
        Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap<>();

        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                //noinspection RedundantConditionalExpression
                resolvedVersioned.put(Pair.of(version,



                                false), resolvedMigration);
            } else {
                resolvedRepeatable.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }

        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList<>();
        List<AppliedMigration> appliedRepeatable = new ArrayList<>();
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





            appliedVersioned.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
        }

        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            MigrationVersion version = av.getLeft().getVersion();
            if (version != null) {
                if (version.compareTo(context.lastApplied) > 0) {



                        context.lastApplied = version;



                } else {
                    av.getRight().outOfOrder = true;
                }
            }
        }

        if (MigrationVersion.CURRENT == target) {
            context.target = context.lastApplied;
        }

        List<MigrationInfoImpl> migrationInfos1 = new ArrayList<>();
        Set<ResolvedMigration> pendingResolvedVersioned = new HashSet<>(resolvedVersioned.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            ResolvedMigration resolvedMigration = resolvedVersioned.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType().isUndo()));
            if (resolvedMigration != null



            ) {
                pendingResolvedVersioned.remove(resolvedMigration);
            }
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, av.getLeft(), context, av.getRight().outOfOrder



            ));
        }

        for (ResolvedMigration prv : pendingResolvedVersioned) {
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false



            ));
        }


        for (AppliedMigration appliedRepeatableMigration : appliedRepeatable) {
            if (!context.latestRepeatableRuns.containsKey(appliedRepeatableMigration.getDescription())
                    || (appliedRepeatableMigration.getInstalledRank() > context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription()))) {
                context.latestRepeatableRuns.put(appliedRepeatableMigration.getDescription(), appliedRepeatableMigration.getInstalledRank());
            }
        }

        Set<ResolvedMigration> pendingResolvedRepeatable = new HashSet<>(resolvedRepeatable.values());
        for (AppliedMigration appliedRepeatableMigration : appliedRepeatable) {
            ResolvedMigration resolvedMigration = resolvedRepeatable.get(appliedRepeatableMigration.getDescription());
            int latestRank = context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription());
            if (resolvedMigration != null && appliedRepeatableMigration.getInstalledRank() == latestRank && ObjectUtils.nullSafeEquals(appliedRepeatableMigration.getChecksum(), resolvedMigration.getChecksum())) {
                pendingResolvedRepeatable.remove(resolvedMigration);
            }
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false



            ));
        }

        for (ResolvedMigration prr : pendingResolvedRepeatable) {
            migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false



            ));
        }

        Collections.sort(migrationInfos1);
        migrationInfos = migrationInfos1;
    }
























    public MigrationInfo[] all() {
        return migrationInfos.toArray(new MigrationInfoImpl[0]);
    }

    public MigrationInfo current() {
        MigrationInfo current = null;
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()




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




            ) {
                return migrationInfo;
            }
        }

        return null;
    }

    public MigrationInfoImpl[] pending() {
        List<MigrationInfoImpl> pendingMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (MigrationState.PENDING == migrationInfo.getState()) {
                pendingMigrations.add(migrationInfo);
            }
        }

        return pendingMigrations.toArray(new MigrationInfoImpl[0]);
    }

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
     * Retrieves the full set of infos about the migrations resolved on the classpath.
     *
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
     * Retrieves the full set of infos about the migrations that failed.
     *
     * @return The failed migrations. An empty array if none.
     */
    public MigrationInfo[] failed() {
        List<MigrationInfo> failedMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isFailed()) {
                failedMigrations.add(migrationInfo);
            }
        }

        return failedMigrations.toArray(new MigrationInfo[0]);
    }

    /**
     * Retrieves the full set of infos about future migrations applied to the DB.
     *
     * @return The future migrations. An empty array if none.
     */
    public MigrationInfo[] future() {
        List<MigrationInfo> futureMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if ((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS)
                    || (migrationInfo.getState() == MigrationState.FUTURE_FAILED)) {
                futureMigrations.add(migrationInfo);
            }
        }

        return futureMigrations.toArray(new MigrationInfo[0]);
    }

    /**
     * Retrieves the full set of infos about out of order migrations applied to the DB.
     *
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