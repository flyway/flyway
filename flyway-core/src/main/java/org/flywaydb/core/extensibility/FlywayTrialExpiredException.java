package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.license.FlywayLicensingException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

public class FlywayTrialExpiredException extends FlywayLicensingException {
    public FlywayTrialExpiredException(Tier tier, String featureName) {
        super("Your 30 day limited Flyway trial license has expired and is no longer valid. " +
                      "Visit " +
                      FlywayDbWebsiteLinks.TRIAL_UPGRADE +
                      " to upgrade to a full " + tier.getDisplayName() + " license to keep on using " + featureName + ".");
    }
}