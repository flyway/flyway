package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.license.FlywayLicensingException;

public class FlywayInvalidLicenseKeyException extends FlywayLicensingException {
    public FlywayInvalidLicenseKeyException() {
        super("Invalid license key. Ensure flyway.licenseKey is set to a valid Flyway license key" +
                      " (\"FL01\" followed by 512 hex chars)");
    }

    public FlywayInvalidLicenseKeyException(String message, Exception e) {
        super(message, e);
    }
}