package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service for aggregating and retrieving the full migrations.
 *
 * TODO: Not so happy with the name of this class. Suggestions welcome.
 */
public class FullMigrationService {
    /**
     * The temporary storage for the actual full migrations.
     */
    private final Map<MigrationVersion, FullMigration> fullMigrationMap = new TreeMap<MigrationVersion, FullMigration>();

    /**
     * For resolving the available migrations.
     */
    private final MigrationResolver migrationResolver;

    /**
     * For retrieving the applied migrations.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The target version up to which to retrieve the info.
     */
    private final MigrationVersion target;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private final boolean outOfOrder;

    /**
     * Creates a new FullMigrationService.
     *
     * @param migrationResolver For resolving the available migrations.
     * @param metaDataTable For retrieving the applied migrations.
     * @param target            The target version up to which to retrieve the info.
     * @param outOfOrder        Allows migrations to be run "out of order".
     */
    public FullMigrationService(MigrationResolver migrationResolver, MetaDataTable metaDataTable,
                                MigrationVersion target, boolean outOfOrder) {
        this.migrationResolver = migrationResolver;
        this.metaDataTable = metaDataTable;
        this.target = target;
        this.outOfOrder = outOfOrder;

        refresh();
    }

    /**
     * Refreshes the migration infos.
     */
    public void refresh() {
        fullMigrationMap.clear();

        List<ResolvedMigration> migrationInfos = migrationResolver.resolveMigrations();
        List<MigrationInfoImpl> appliedMigrations = metaDataTable.allAppliedMigrations();
    }
}
