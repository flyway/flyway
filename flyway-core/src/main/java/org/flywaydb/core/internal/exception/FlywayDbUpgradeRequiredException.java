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
package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;

/**
 * Thrown when an attempt was made to migrate an outdated database version not supported by Flyway.
 */
public class FlywayDbUpgradeRequiredException extends FlywayException {
    public FlywayDbUpgradeRequiredException(String database, String version, String minimumVersion) {
        super(database + " upgrade required: " + database + " " + version
                + " is outdated and no longer supported by Flyway. Flyway currently supports " + database + " "
                + minimumVersion + " and newer.");
    }
}