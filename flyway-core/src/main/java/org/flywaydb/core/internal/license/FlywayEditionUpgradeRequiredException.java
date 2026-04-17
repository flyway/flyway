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
package org.flywaydb.core.internal.license;

import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

public class FlywayEditionUpgradeRequiredException extends FlywayLicensingException {
    public FlywayEditionUpgradeRequiredException(final Tier current, final String feature) {
        super("Upgrade required: " + feature + " is not supported by " +
            (current == null ? "OSS" : current.getDisplayName())
            + "."
            + (current != null
            ? " If you would like to start a free Enterprise trial, please run auth -startEnterpriseTrial -IAgreeToTheEula."
            + " If you believe you should be logged in, run auth -IAgreeToTheEula or ensure licensing is set correctly."
            : " Download Redgate Edition for free: "
                + FlywayDbWebsiteLinks.REDGATE_EDITION_DOWNLOAD
                + "."
                + " Once you have installed Redgate Edition, you can start a free Enterprise trial by running auth -startEnterpriseTrial -IAgreeToTheEula."));
    }
}
