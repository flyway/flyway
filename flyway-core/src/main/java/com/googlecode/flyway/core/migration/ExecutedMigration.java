package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationVersion;

import java.util.Date;

/**
 * A migration applied to the database.
 */
public class ExecutedMigration {
    /**
     * The target version of this migration.
     */
    private MigrationVersion version;

    /**
     * The description of the migration.
     */
    private String description;

    /**
     * The name of the script to execute for this migration, relative to its classpath location.
     */
    private String script;

    /**
     * The checksum of the migration.
     */
    private Integer checksum;

    /**
     * The type of migration (INIT, SQL, ...)
     */
    private com.googlecode.flyway.core.api.MigrationType migrationType;

    /**
     * The state of the migration (PENDING, SUCCESS, ...)
     */
    private com.googlecode.flyway.core.api.MigrationState migrationState = com.googlecode.flyway.core.api.MigrationState.PENDING;

    /**
     * The timestamp when this migration was installed.
     */
    private Date installedOn;

    /**
     * The execution time (in millis) of this migration.
     */
    private Integer executionTime;
}
