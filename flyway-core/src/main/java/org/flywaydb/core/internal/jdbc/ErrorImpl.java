package org.flywaydb.core.internal.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.Getter;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;

import java.util.List;

@RequiredArgsConstructor
@Getter(onMethod = @__(@Override))
public class ErrorImpl implements Error {
    private final int code;
    private final String state;
    private final String message;
    private boolean handled;

    @Override
    public void setHandled(boolean handled, Configuration configuration) {
        if (!LicenseGuard.isLicensed(configuration, Tier.PREMIUM)) {
            throw new FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(configuration), "Error handling");
        }

        this.handled = handled;
    }
}