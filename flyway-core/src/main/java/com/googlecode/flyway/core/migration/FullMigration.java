package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationVersion;

/**
 * Complete info about a migration, aggregated from all sources (classpath, DB, ...)
 *
 * TODO: Not so happy with the name of this class. Suggestions welcome.
 */
public class FullMigration implements Comparable<FullMigration> {
    /**
     * The version of this migration.
     */
    private final MigrationVersion version;

    /**
     * The applied migration with this version.
     */
    private ExecutedMigration executedMigration;

    /**
     * The available migration with this version.
     */
    private ResolvedMigration resolvedMigration;

    /**
     * Creates a new FullMigration for this version.
     *
     * @param version The version of this migration.
     */
    public FullMigration(MigrationVersion version) {
        this.version = version;
    }

    /**
     * @return The version of this migration.
     */
    public MigrationVersion getVersion() {
        return version;
    }

    /**
     * @param executedMigration The applied migration with this version.
     */
    public void setExecutedMigration(ExecutedMigration executedMigration) {
        this.executedMigration = executedMigration;
    }

    /**
     * @param resolvedMigration The available migration with this version.
     */
    public void setResolvedMigration(ResolvedMigration resolvedMigration) {
        this.resolvedMigration = resolvedMigration;
    }

    public int compareTo(FullMigration o) {
        return version.compareTo(o.version);
    }
}
