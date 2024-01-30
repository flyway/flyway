package org.flywaydb.core.internal.license;

import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

public class FlywayEditionUpgradeRequiredException extends FlywayLicensingException {
    public FlywayEditionUpgradeRequiredException(Tier edition, DatabaseType databaseType, String version) {
        super(edition + " or " + databaseType.getName() + " upgrade required: " + databaseType.getName() + " " + version
                      + " is no longer supported by your current edition of Flyway,"
                      + " but still supported by " + (edition == null ? "OSS" : edition.getDisplayName()) + ".");
    }

    public FlywayEditionUpgradeRequiredException(Tier required, Tier current, String feature) {
        super(required.getDisplayName() + " upgrade required: " + feature + " is not supported by " + (current == null ? "OSS" : current.getDisplayName()) + "." + (
            current != null
            ? " If you would like to start a free " + required.getDisplayName() + " trial, please run auth " +
                ("Enterprise".equalsIgnoreCase(required.name()) ? "-startEnterpriseTrial" : "-startTeamsTrial") + " -IAgreeToTheEula."
            : " Download Redgate Edition for free: " + FlywayDbWebsiteLinks.REDGATE_EDITION_DOWNLOAD + "." +
              " Once you have installed Redgate Edition, you can start a free " + required.getDisplayName() + " trial by running auth " +
                ("Enterprise".equalsIgnoreCase(required.name()) ? "-startEnterpriseTrial" : "-startTeamsTrial") + " -IAgreeToTheEula."));
    }
}