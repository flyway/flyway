/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.extensibility;

import lombok.CustomLog;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.license.FlywayPermit;






import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

        throw new FlywayEditionUpgradeRequiredException(editions.get(0), flywayPermit.getTier(), featureName);
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

    public static FlywayPermit getPermit(Configuration configuration) {
        return getPermit(configuration, true);
    }

    public static Tier getTier(Configuration configuration) {
        return getPermit(configuration, true).getTier();
    }

    public static String getTierAsString(Configuration configuration) {
        return getPermit(configuration, true).getTier().asString();
    }

    public static FlywayPermit getPermit(Configuration configuration, boolean fromCache) {

























         return new FlywayPermit("Anonymous", null, null, false, false);

    }

    public static void dropCache() {











    }
}