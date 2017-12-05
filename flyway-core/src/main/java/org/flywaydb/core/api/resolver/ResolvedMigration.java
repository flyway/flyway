/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;

/**
 * Migration resolved through a MigrationResolver. Can be applied against a database.
 */
public interface ResolvedMigration {
    /**
     * @return The version of the database after applying this migration. {@code null} for repeatable migrations.
     */
    MigrationVersion getVersion();

    /**
     * @return The description of the migration.
     */
    String getDescription();

    /**
     * @return The name of the script to execute for this migration, relative to its base (classpath/filesystem) location.
     */
    String getScript();

    /**
     * @return The checksum of the migration. Optional. Can be {@code null} if not unique checksum is computable.
     */
    Integer getChecksum();

    /**
     * @return The type of migration (INIT, SQL, ...)
     */
    MigrationType getType();

    /**
     * @return The physical location of the migration on disk. Used for more precise error reporting in case of conflict.
     */
    String getPhysicalLocation();

    /**
     * @return The executor to run this migration.
     */
    MigrationExecutor getExecutor();
}
