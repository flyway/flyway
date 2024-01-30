package org.flywaydb.core.internal.license;

import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

/**
 * Thrown when an attempt was made to use a Flyway Teams Edition feature not supported by
 * Flyway Community Edition.
 */
public class FlywayRedgateEditionRequiredException extends FlywayLicensingException {
    public FlywayRedgateEditionRequiredException(String feature) {
        super("Flyway Redgate Edition Required: " + feature + " is not supported by OSS Edition\n" +
                      "Download Redgate Edition for free: " + FlywayDbWebsiteLinks.REDGATE_EDITION_DOWNLOAD);
    }
}