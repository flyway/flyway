package org.flywaydb.core.internal.license;

import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

public class FlywayTeamsUpgradeMessage {
    public static String generate(String detectedFeature, String usageMessage) {
        return "Detected " + detectedFeature + ". " +
                "Upgrade to " + Tier.TEAMS.getDisplayName() + " to " + usageMessage + ". Try " + Tier.TEAMS.getDisplayName() + " " +
                "for free: " + FlywayDbWebsiteLinks.TRY_TEAMS_EDITION;
    }
}