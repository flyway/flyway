/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.license;

import java.io.File;
import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.Tier;


import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

@Getter
@CustomLog
public class FlywayPermit implements Serializable {
    private String owner;
    private Date permitExpiry;
    private Date contractExpiry;
    private Tier tier;
    private boolean trial;
    private boolean redgateEmployee;
    private boolean fromAuth;
    private final long DAYS_TO_DISPLAY_LICENSED_UNTIL = 30;
    static final long PERMIT_FILE_OUTDATED_TIME = 24 * 60 * 60 * 1000;
    private static final File FLYWAY_APP_DATA_FOLDER = FileUtils.getAppDataFlywayCLILocation();
    private static final File PERMIT_FILE = new File(FLYWAY_APP_DATA_FOLDER, "permit");
    private static final File REFRESH_TOKEN_FILE = new File(FLYWAY_APP_DATA_FOLDER, "refresh_token");




































    public FlywayPermit(String owner, Date permitExpiry, Date contractExpiry, boolean trial, boolean redgateEmployee, boolean fromAuth) {
        this.owner = owner;
        this.permitExpiry = permitExpiry;
        this.contractExpiry = contractExpiry;
        this.trial = trial;
        this.redgateEmployee = redgateEmployee;
        this.fromAuth = fromAuth;
    }

    public void print() {
        if (this.tier == null) {
            LOG.info("Flyway OSS Edition " + VersionPrinter.getVersion() + " by Redgate");
            LOG.info("");
            return;
        } else {
            LOG.info("Flyway " + this.tier.getDisplayName() + " Edition " + VersionPrinter.getVersion() + " by Redgate");
        }

        if (contractExpiry != null) {
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
        }

        if (!REFRESH_TOKEN_FILE.exists() && PERMIT_FILE.exists() && fromAuth) {
            if (permitFileOutdated(PERMIT_FILE)) {
                LOG.info("Flyway permit on disk is outdated and cannot be refreshed automatically because there is no refresh token on disk. Please rerun auth");
            } else if (permitExpired()) {
                LOG.info("Flyway permit on disk is expired and cannot be refreshed automatically because there is no refresh token on disk. Please rerun auth");
            }
        }

        if (this.tier == Tier.COMMUNITY && PERMIT_FILE.exists()) {
            LOG.info("No Flyway license detected for this user - using Community Edition. If you expected a Teams/Enterprise license then please add a Flyway license to your account and rerun auth. Alternatively, you can run auth -logout to remove your unlicensed permit on disk");
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

    public static boolean permitFileOutdated(File permitFile) {
        return permitFile.lastModified() + PERMIT_FILE_OUTDATED_TIME < new Date().getTime();
    }

    public boolean permitExpired() {
        return permitExpiry == null || permitExpiry.before(new Date());
    }

    public boolean contractExpired() {
        return contractExpiry == null || contractExpiry.before(new Date());
    }
}
