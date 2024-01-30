package org.flywaydb.core.internal.license;

import org.flywaydb.core.extensibility.Tier;

public class FlywayExpiredLicenseKeyException extends FlywayLicensingException {
    public FlywayExpiredLicenseKeyException(Tier tier) {
        super("Your " + tier.getDisplayName() + " license has expired and is no longer valid." +
                      " You must renew your license immediately to keep on using this software.");
    }
}