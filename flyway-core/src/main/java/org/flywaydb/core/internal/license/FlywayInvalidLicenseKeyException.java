package org.flywaydb.core.internal.license;

public class FlywayInvalidLicenseKeyException extends FlywayLicensingException {
    public FlywayInvalidLicenseKeyException() {
        super("An invalid Flyway license key was provided; fell back to Community Edition. " +
                      "Remove license key and run auth to authorize online. Please contact sales at sales@flywaydb.org for license information.");
    }

    public FlywayInvalidLicenseKeyException(String message, Exception e) {
        super(message, e);
    }
}