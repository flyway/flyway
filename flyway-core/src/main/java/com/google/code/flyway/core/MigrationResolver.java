package com.google.code.flyway.core;

import java.util.Collection;

/**
 * Facility for resolving available migrations.
 */
public interface MigrationResolver {
    /**
     * Resolves the available migrations.
     *
     * @return The available migrations.
     */
    Collection<Migration> resolvesMigrations();
}
