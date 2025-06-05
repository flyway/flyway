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
            throw new FlywayEditionUpgradeRequiredException(LicenseGuard.getTier(configuration), "Warning handling");
        }

        this.handled = handled;
    }
}
