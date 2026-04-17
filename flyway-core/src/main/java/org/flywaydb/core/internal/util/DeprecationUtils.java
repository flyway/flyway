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
package org.flywaydb.core.internal.util;

import static org.flywaydb.core.internal.util.VersionUtils.currentVersionIsHigherThanOrEquivalentTo;

import java.lang.module.ModuleDescriptor.Version;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.license.VersionPrinter;

@CustomLog
public class DeprecationUtils {
    public enum DeprecatedFeatures {
        CLONE_PROVISIONER("Clone Provisioner", null, 13),
        CLONE_RESOLVER("Clone Resolver", null, 13),
        CHECK_BUILD_URL("check.buildUrl", "check.buildEnvironment", null),
        CREATE_SCHEMA("'createSchema' callback", "beforeCreateSchema", null),
        MONGODB_URL("jdbc:mongodb:// URL prefix", "mongodb://", null);

        private final String feature;
        private final String replacement;
        private final Integer version;

        DeprecatedFeatures(final String feature, final String replacement, final Integer removalVersion) {
            this.feature = feature;
            this.replacement = replacement;
            this.version = removalVersion;
        }
    }

    public static void throwUnsupportedIfLaterThanOrEquivalentTo(final DeprecatedFeatures feature) {
        final String versionOverride = System.getenv("FLYWAY_NEXT_MAJOR_VERSION");
        final Version currentVersion = Version.parse(versionOverride != null
            ? versionOverride
            : VersionPrinter.getVersion());
        if (feature.version != null && currentVersionIsHigherThanOrEquivalentTo(currentVersion,
            Version.parse(String.valueOf(feature.version)))) {
            throw new FlywayException(feature.feature + " is no longer supported");
        } else {
            printDeprecationNotice(feature);
        }
    }

    public static void printDeprecationNotice(final DeprecatedFeatures feature) {
        final String message = feature.feature
            + " is deprecated and will be removed in a future release."
            + (feature.replacement != null ? "Please use " + feature.replacement + " instead" : "");
        LOG.warn(message);
    }
}
