package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.exception.FlywayException;

/**
 * Exception indicating that migration failed.
 */
public class MigrationException extends FlywayException {
    /**
     * The version of the migration that failed.
     */
    private final SchemaVersion version;

    /**
     * Flag indicating whether a rollback was performed or not.
     */
    private final boolean rollback;

    /**
     * Creates a new MigrationException for this version.
     *
     * @param version The version of the migration that failed.
     * @param rollback Flag indicating whether a rollback was performed or not.
     */
    public MigrationException(SchemaVersion version, boolean rollback) {
        super();
        this.version = version;
        this.rollback = rollback;
    }

    @Override
    public String getMessage() {
        if (rollback) {
            return "Migration to version " + version + " failed! Changes successfully rolled back.";
        }

        return "Migration to version " + version + " failed! Please restore backups and roll back database and code!";
    }
}
