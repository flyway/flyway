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
package org.flywaydb.core.api;

/**
 * Type of migration.
 */
public enum MigrationType {
    /**
     * Schema creation migration.
     */
    SCHEMA(true, false),

    /**
     * Bseline migration.
     */
    BASELINE(true, false),

    /**
     * SQL migrations.
     */
    SQL(false, false),

    /**
     * Undo SQL migrations.
     */
    UNDO_SQL(false, true),

    /**
     * JDBC Java-based migrations.
     */
    JDBC(false, false),

    /**
     * Undo JDBC java-based migrations.
     */
    UNDO_JDBC(false, true),

    /**
     * Spring JDBC Java-based migrations.
     */
    SPRING_JDBC(false, false),

    /**
     * Undo Spring JDBC java-based migrations.
     */
    UNDO_SPRING_JDBC(false, true),

    /**
     * Migrations using custom MigrationResolvers.
     */
    CUSTOM(false, false),

    /**
     * Undo migrations using custom MigrationResolvers.
     */
    UNDO_CUSTOM(false, true);

    private final boolean synthetic;
    private final boolean undo;

    MigrationType(boolean synthetic, boolean undo) {
        this.synthetic = synthetic;
        this.undo = undo;
    }

    /**
     * @return Whether this is a synthetic migration type, which is only ever present in the schema history table,
     * but never discovered by migration resolvers.
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * @return Whether this is an undo migration, which has undone an earlier migration present in the schema history table.
     */
    public boolean isUndo() {
        return undo;
    }
}
