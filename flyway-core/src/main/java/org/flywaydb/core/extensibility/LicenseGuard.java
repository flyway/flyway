/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.extensibility;

import lombok.CustomLog;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.license.FlywayPermit;

import java.util.Date;
import java.util.List;

@CustomLog
@ExtensionMethod(Tier.class)
public class LicenseGuard {
    public static void guard(Configuration configuration, List<Tier> editions, String featureName) {
        FlywayPermit flywayPermit = getPermit(configuration);
        if ((flywayPermit.getPermitExpiry() != null && flywayPermit.getPermitExpiry().before(new Date())) ||
                (flywayPermit.getContractExpiry() != null && flywayPermit.getContractExpiry().before(new Date()))) {
            if (flywayPermit.isTrial()) {
                throw new FlywayTrialExpiredException(flywayPermit.getTier(), featureName);
            } else {
                throw new FlywayExpiredLicenseKeyException(flywayPermit.getTier(), featureName);
            }
        }

        for (Tier tier : editions) {
            if (flywayPermit.getTier() == tier) {
                return;
            }
        }

        throw new FlywayEditionUpgradeRequiredException(flywayPermit.getTier(), featureName);
    }

    public static boolean isLicensed(Configuration configuration, List<Tier> editions) {
        FlywayPermit flywayPermit = getPermit(configuration);
        if ((flywayPermit.getPermitExpiry() != null && flywayPermit.getPermitExpiry().before(new Date())) ||
                (flywayPermit.getContractExpiry() != null && flywayPermit.getContractExpiry().before(new Date()))) {
            return false;
        }

        for (Tier tier : editions) {
            if (flywayPermit.getTier() == tier) {
                return true;
            }
        }

        return false;
    }

    public static void submitPur(Configuration configuration, String eventType, Database database) {
        configuration.getPluginRegister().getInstanceOf(LicenseSupport.class).submitPur(configuration, eventType, database);
    }

    public static FlywayPermit getPermit(Configuration configuration) {
        return getPermit(configuration, true);
    }

    public static FlywayPermit getPermitNoCache(Configuration configuration) {
        return getPermit(configuration, false);
    }

    public static Tier getTier(Configuration configuration) {
        return getPermit(configuration).getTier();
    }

    public static String getTierAsString(Configuration configuration) {
        return getPermit(configuration).getTier().asString();
    }

    private static FlywayPermit getPermit(Configuration configuration, boolean fromCache) {
        return configuration.getPluginRegister().getInstanceOf(LicenseSupport.class).getPermit(configuration, fromCache);
    }

    public static List<String> consumeDeferredWarnings(Configuration configuration) {
        return configuration.getPluginRegister().getInstanceOf(LicenseSupport.class).consumeDeferredWarnings(configuration);
    }
}
