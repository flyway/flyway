/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.DatabaseType;

/**
 * Thrown when an attempt was made to migrate an older database version no longer supported by this Flyway edition.
 */
public class FlywayEditionUpgradeRequiredException extends FlywayException {
    public FlywayEditionUpgradeRequiredException(Edition edition, DatabaseType databaseType, String version) {
        super(edition + " or " + databaseType + " upgrade required: " + databaseType + " " + version
                + " is no longer supported by " + VersionPrinter.EDITION + ","
                + " but still supported by " + edition + ".");
    }
}