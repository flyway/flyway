package com.googlecode.flyway.core.api;

import java.util.Date;

/**
 * Info about a migration.
 */
public interface MigrationInfo extends Comparable<MigrationInfo> {
    /**
     * @return The type of migration (INIT, SQL or JAVA)
     */
    MigrationType getType();

    /**
     * @return The target version of this migration.
     */
    Integer getChecksum();

    /**
     * @return The schema version after the migration is complete.
     */
    MigrationVersion getVersion();

    /**
     * @return The description of the migration.
     */
    String getDescription();

    /**
     * @return The name of the script to execute for this migration, relative to its classpath location.
     */
    String getScript();

    /**
     * @return The state of the migration (PENDING, SUCCESS, ...)
     */
    MigrationState getState();

    /**
     * @return The timestamp when this migration was installed. (Only for applied migrations)
     */
    Date getInstalledOn();

    /**
     * @return The execution time (in millis) of this migration. (Only for applied migrations)
     */
    Integer getExecutionTime();
}
