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

/**
 * Migration implementors that also implement this interface will be able to specify their checksum (for
 * validation), instead of having it automatically computed or default to {@code null} (for Java Migrations).
 */
public interface MigrationChecksumProvider {
    /**
     * Computes the checksum of the migration.
     *
     * @return The checksum of the migration.
     */
    Integer getChecksum();
}
