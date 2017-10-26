package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.api.FlywayException;

/**
 * Thrown when an attempt was made to migrate an outdated database version not supported by Flyway.
 */
public class FlywayDbUpgradeRequiredException extends FlywayException {
    public FlywayDbUpgradeRequiredException(String database, String version, String minimumVersion) {
        super(database + " upgrade required: " + database + " " + version
                + " is outdated and no longer supported by Flyway. Flyway currently supports " + database + " "
                + minimumVersion + " and newer.");
    }
}
