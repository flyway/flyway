package com.googlecode.flyway.core.api;

/**
 * Info about all migrations, including applied, current and pending with details and status.
 */
public interface MigrationInfos {
    /**
     * Retrieves the full set of infos about applied, current and future migrations.
     *
     * @return The full set of infos. An empty array if none.
     */
    MigrationInfo[] all();

    /**
     * Retrieves the information of the current applied migration, if any.
     *
     * @return The info. {@code null} if no migrations have been applied yet.
     */
    MigrationInfo current();

    /**
     * Retrieves the information of the failed migration, if any.
     *
     * @return The info. {@code null} all migrations were successful.
     */
    MigrationInfo failed();

    /**
     * Retrieves the full set of infos about pending migrations, available locally, but not yet applied to the DB.
     *
     * @return The pending migrations. An empty array if none.
     */
    MigrationInfo[] pending();
}
