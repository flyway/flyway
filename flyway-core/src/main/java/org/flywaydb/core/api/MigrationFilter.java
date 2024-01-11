package org.flywaydb.core.api;

public interface MigrationFilter {
    boolean matches(MigrationInfo info);
}