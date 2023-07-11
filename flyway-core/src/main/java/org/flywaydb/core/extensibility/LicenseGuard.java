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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;

import java.util.List;

public class LicenseGuard {
    public static void guard(Configuration configuration, List<Edition> editions, String featureName) {
        try {
            LicenseInfo licenseInfo = LicenseInfo.create(configuration.getLicenseKey());
            if (!editions.contains(licenseInfo.getLicenseType().getEdition())) {
                throw new FlywayEditionUpgradeRequiredException(editions.get(0), licenseInfo.getLicenseType().getEdition(), featureName);
            }

            if (licenseInfo.isExpired()) {
                if (licenseInfo.isTrial()) {
                    throw new FlywayTrialExpiredException(licenseInfo.getLicenseType().getEdition(), featureName);
                } else {
                    throw new FlywayExpiredLicenseKeyException(licenseInfo.getLicenseType().getEdition(), featureName);
                }
            }
        } catch (FlywayMissingLicenseKeyException e) {
            throw new FlywayMissingLicenseKeyException(featureName, e);
        }
    }
}