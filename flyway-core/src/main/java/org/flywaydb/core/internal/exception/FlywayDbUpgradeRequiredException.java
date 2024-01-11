package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseType;

/**
 * Thrown when an attempt was made to migrate an outdated database version not supported by Flyway.
 */
public class FlywayDbUpgradeRequiredException extends FlywayException {
    public FlywayDbUpgradeRequiredException(DatabaseType databaseType, String version, String minimumVersion) {
        super(databaseType.getName() + " upgrade required: " + databaseType.getName() + " " + version
                      + " is outdated and no longer supported by Flyway. Flyway currently supports " + databaseType.getName() + " "
                      + minimumVersion + " and newer.");
    }
}