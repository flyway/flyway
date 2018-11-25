/*
 * Copyright 2010-2018 Boxfuse GmbH
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

/**
 * Thrown when an attempt was made to migrate an older database version no longer enjoying regular support by its
 * vendor and no longer supported by Flyway Community Edition and Flyway Pro Edition.
 */
public class FlywayEnterpriseUpgradeRequiredException extends FlywayException {
    public FlywayEnterpriseUpgradeRequiredException(String vendor, String database, String version) {
        super(Edition.ENTERPRISE + " or " + database + " upgrade required: " + database + " " + version
                + " is past regular support by " + vendor
                + " and no longer supported by " + VersionPrinter.EDITION + ","
                + " but still supported by Flyway Enterprise Edition.");
    }
}