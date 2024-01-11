package org.flywaydb.core.internal.license;

public class FlywayRedgateLicenseKeyException extends FlywayLicensingException {
    public FlywayRedgateLicenseKeyException() {
        super("A Redgate license key was provided; fell back to Community Edition. " +
                      "Please contact sales at sales@flywaydb.org for license information.");
    }
}