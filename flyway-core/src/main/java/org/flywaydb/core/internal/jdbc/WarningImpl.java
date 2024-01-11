package org.flywaydb.core.internal.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;

@RequiredArgsConstructor
@Getter(onMethod = @__(@Override))
public class WarningImpl implements Warning {
    private final int code;
    private final String state;
    private final String message;
    private boolean handled;

    @Override
    public void setHandled(boolean handled, Configuration configuration) {
        if (!LicenseGuard.isLicensed(configuration, Tier.PREMIUM)) {
            throw new FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(configuration), "Warning handling");
        }

        this.handled = handled;
    }
}