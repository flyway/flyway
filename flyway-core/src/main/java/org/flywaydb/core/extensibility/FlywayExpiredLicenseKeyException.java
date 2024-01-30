package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.license.FlywayLicensingException;

public class FlywayExpiredLicenseKeyException extends FlywayLicensingException {

    public FlywayExpiredLicenseKeyException(Tier tier, String featureName) {
        super("Your " + tier.getDisplayName() + " license has expired and is no longer valid." +
                      " You must renew your license immediately to keep on using " + featureName + ".");
    }
}