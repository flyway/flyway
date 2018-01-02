/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.MigrationVersion;

/**
 * Migration implementors that also implement this interface will be able to specify their version and description
 * manually, instead of having it automatically computed.
 */
public interface MigrationInfoProvider {
    /**
     * Returns the version after the migration is complete.
     *
     * @return The version after the migration is complete. {@code null} for repeatable migrations.
     */
    MigrationVersion getVersion();

    /**
     * Returns the description for the migration history.
     *
     * @return The description for the migration history. Never {@code null}.
     */
    String getDescription();

    /**
     * Whether this is an undo migration for a previously applied versioned migration.
     *
     * @return {@code true} if it is, {@code false} if not. Always {@code false} for repeatable migrations.
     */
    boolean isUndo();
}
