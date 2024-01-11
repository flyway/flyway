package org.flywaydb.core.internal.license;

import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.Tier;


import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

@Getter
@CustomLog
public class FlywayPermit implements Serializable {
    private String owner;
    private Date permitExpiry;
    private Date contractExpiry;
    private Tier tier;
    private boolean trial;
    private boolean redgateEmployee;
    private final long DAYS_TO_DISPLAY_LICENSED_UNTIL = 30;


































    public FlywayPermit(String owner, Date permitExpiry, Date contractExpiry, boolean trial, boolean redgateEmployee) {
        this.owner = owner;
        this.permitExpiry = permitExpiry;
        this.contractExpiry = contractExpiry;
        this.trial = trial;
        this.redgateEmployee = redgateEmployee;
    }

    public void print() {
        if (this.tier == null) {
            LOG.info("Flyway OSS Edition " + VersionPrinter.getVersion() + " by Redgate");
        } else {
            LOG.info("Flyway " + this.tier.getDisplayName() + " Edition " + VersionPrinter.getVersion() + " by Redgate");
        }
        if ("Online User".equals(this.owner)) {
            if (this.contractExpiry.getTime() == Long.MAX_VALUE) {
                LOG.debug("License has no expiry date");
            } else {
                logLicensedUntilIfWithinWindow();
            }
        } else if (!"Anonymous".equals(this.owner)) {
            LOG.info("Licensed to " + this.owner);
            logLicensedUntilIfWithinWindow();
        }

        if (isTrial()) {
            LOG.warn("You are using a limited Flyway trial license, valid until " + DateUtils.toDateString(this.contractExpiry) + "." +
                             " In " + StringUtils.getDaysString(DateUtils.getRemainingDays(this.contractExpiry)) +
                             " you must either upgrade to a full " + this.tier.getDisplayName() + " license or downgrade to " + Tier.COMMUNITY.getDisplayName() + ".");
        }
        LOG.info("");
    }

    private void logLicensedUntilIfWithinWindow() {
        if (DateUtils.getRemainingDays(this.contractExpiry) <= DAYS_TO_DISPLAY_LICENSED_UNTIL) {
            LOG.info("Licensed until " + DateUtils.toDateString(this.contractExpiry) + " (" + StringUtils.getDaysString(DateUtils.getRemainingDays(this.contractExpiry)) + " remaining)");
        }
    }

    public boolean permitExpired() {
        return permitExpiry == null || permitExpiry.before(new Date());
    }

    public boolean contractExpired() {
        return contractExpiry == null || contractExpiry.before(new Date());
    }
}