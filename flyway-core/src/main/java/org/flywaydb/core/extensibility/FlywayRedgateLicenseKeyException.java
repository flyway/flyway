package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.license.FlywayLicensingException;

public class FlywayRedgateLicenseKeyException extends FlywayLicensingException {
    public FlywayRedgateLicenseKeyException() {
        super("Invalid license key. You have provided a Redgate license key. Please contact sales@flywaydb.org to" +
                      " acquire a Flyway license key (\"FL01\" followed by 512 hex chars)");
    }
}