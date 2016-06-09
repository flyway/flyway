/**
 * Copyright 2010-2016 Boxfuse GmbH
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

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
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
     * The metadata table for applied migrations.
     */
    private final MetaDataTable metaDataTable;

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
     * @param metaDataTable     The metadata table for applied migrations.
     * @param target            The target version up to which to retrieve the info.
     * @param outOfOrder        Allows migrations to be run "out of order".
     * @param pending           Whether pending migrations are allowed.
     * @param future            Whether future migrations are allowed.
     */
    public MigrationInfoServiceImpl(MigrationResolver migrationResolver, MetaDataTable metaDataTable,
                                    MigrationVersion target, boolean outOfOrder, boolean pending, boolean future) {
        this.migrationResolver = migrationResolver;
        this.metaDataTable = metaDataTable;
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.pending = pending;
        this.future = future;
    }

    /**
     * Refreshes the info about all known migrations from both the classpath and the DB.
     */
    public void refresh() {
        Collection<ResolvedMigration> availableMigrations = migrationResolver.resolveMigrations();
        List<AppliedMigration> appliedMigrations = metaDataTable.allAppliedMigrations();

        migrationInfos = mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        if (MigrationVersion.CURRENT == target) {
            MigrationInfo current = current();
            if (current == null) {
                target = MigrationVersion.EMPTY;
            } else {
                target = current.getVersion();
            }
        }
    }

    /**
     * Merges the available and the applied migrations to produce one fully aggregated and consolidated list.
     *
     * @param resolvedMigrations The available migrations.
     * @param appliedMigrations  The applied migrations.
     * @return The complete list of migrations.
     */
    private List<MigrationInfoImpl> mergeAvailableAndAppliedMigrations(Collection<ResolvedMigration> resolvedMigrations, List<AppliedMigration> appliedMigrations) {
        MigrationInfoContext context = new MigrationInfoContext();
        context.outOfOrder = outOfOrder;
        context.pending = pending;
        context.future = future;
        context.target = target;

        Map<MigrationVersion, ResolvedMigration> resolvedMigrationsMap = new TreeMap<MigrationVersion, ResolvedMigration>();
        Map<String, ResolvedMigration> resolvedRepeatableMigrationsMap = new TreeMap<String, ResolvedMigration>();
        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                resolvedMigrationsMap.put(version, resolvedMigration);
            } else {
                resolvedRepeatableMigrationsMap.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }

        Map<MigrationVersion, Pair<AppliedMigration, Boolean>> appliedMigrationsMap =
                new TreeMap<MigrationVersion, Pair<AppliedMigration, Boolean>>();
        List<AppliedMigration> appliedRepeatableMigrations = new ArrayList<AppliedMigration>();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            MigrationVersion version = appliedMigration.getVersion();
            boolean outOfOrder = false;
            if (version != null) {
                if (version.compareTo(context.lastApplied) > 0) {
                    context.lastApplied = version;
                } else {
                    outOfOrder = true;
                }
            }
            if (appliedMigration.getType() == MigrationType.SCHEMA) {
                context.schema = version;
            }
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                context.baseline = version;
            }
            if (version != null) {
                appliedMigrationsMap.put(version, Pair.of(appliedMigration, outOfOrder));
            } else {
                appliedRepeatableMigrations.add(appliedMigration);
            }
        }

        Set<MigrationVersion> allVersions = new HashSet<MigrationVersion>();
        allVersions.addAll(resolvedMigrationsMap.keySet());
        allVersions.addAll(appliedMigrationsMap.keySet());

        List<MigrationInfoImpl> migrationInfos = new ArrayList<MigrationInfoImpl>();
        for (MigrationVersion version : allVersions) {
            ResolvedMigration resolvedMigration = resolvedMigrationsMap.get(version);
            Pair<AppliedMigration, Boolean> appliedMigrationInfo = appliedMigrationsMap.get(version);
            if (appliedMigrationInfo == null) {
                migrationInfos.add(new MigrationInfoImpl(resolvedMigration, null, context, false));
            } else {
                migrationInfos.add(new MigrationInfoImpl(resolvedMigration, appliedMigrationInfo.getLeft(), context, appliedMigrationInfo.getRight()));
            }
        }

        Set<ResolvedMigration> pendingResolvedRepeatableMigrations = new HashSet<ResolvedMigration>(resolvedRepeatableMigrationsMap.values());
        for (AppliedMigration appliedRepeatableMigration : appliedRepeatableMigrations) {
            ResolvedMigration resolvedMigration = resolvedRepeatableMigrationsMap.get(appliedRepeatableMigration.getDescription());
            if (resolvedMigration != null && ObjectUtils.nullSafeEquals(appliedRepeatableMigration.getChecksum(), resolvedMigration.getChecksum())) {
                pendingResolvedRepeatableMigrations.remove(resolvedMigration);
            }
            if (!context.latestRepeatableRuns.containsKey(appliedRepeatableMigration.getDescription())
                    || (appliedRepeatableMigration.getInstalledRank() > context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription()))) {
                context.latestRepeatableRuns.put(appliedRepeatableMigration.getDescription(), appliedRepeatableMigration.getInstalledRank());
            }
            migrationInfos.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false));
        }

        for (ResolvedMigration pendingResolvedRepeatableMigration : pendingResolvedRepeatableMigrations) {
            migrationInfos.add(new MigrationInfoImpl(pendingResolvedRepeatableMigration, null, context, false));
        }

        Collections.sort(migrationInfos);

        return migrationInfos;
    }

    public MigrationInfo[] all() {
        return migrationInfos.toArray(new MigrationInfoImpl[migrationInfos.size()]);
    }

    public MigrationInfo current() {
        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            MigrationInfo migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isApplied() && migrationInfo.getVersion() != null) {
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

    public MigrationInfo[] applied() {
        List<MigrationInfo> appliedMigrations = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()) {
                appliedMigrations.add(migrationInfo);
            }
        }

        return appliedMigrations.toArray(new MigrationInfo[appliedMigrations.size()]);
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
