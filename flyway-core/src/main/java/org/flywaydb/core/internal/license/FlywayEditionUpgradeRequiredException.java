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
package org.flywaydb.core.internal.license;

import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.DatabaseType;

public class FlywayEditionUpgradeRequiredException extends FlywayLicensingException {
    public FlywayEditionUpgradeRequiredException(Tier edition, DatabaseType databaseType, String version) {
        super(edition + " or " + databaseType.getName() + " upgrade required: " + databaseType.getName() + " " + version
                      + " is no longer supported by your current edition of Flyway,"
                      + " but still supported by " + (edition == null ? "OSS" : edition.getDisplayName()) + ".");
    }

    public FlywayEditionUpgradeRequiredException(Tier required, Tier current, String feature) {
        super(required.getDisplayName() + " upgrade required: " + feature + " is not supported by " + (current == null ? "OSS" : current.getDisplayName()) + ".");
    }
}