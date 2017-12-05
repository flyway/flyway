/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.api;

/**
 * Info about all migrations, including applied, current and pending with details and status.
 */
public interface MigrationInfoService {
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
     * Retrieves the full set of infos about pending migrations, available locally, but not yet applied to the DB.
     *
     * @return The pending migrations. An empty array if none.
     */
    MigrationInfo[] pending();

    /**
     * Retrieves the full set of infos about the migrations applied to the DB.
     *
     * @return The applied migrations. An empty array if none.
     */
    MigrationInfo[] applied();
}
