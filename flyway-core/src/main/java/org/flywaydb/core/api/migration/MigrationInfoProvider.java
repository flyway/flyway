/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.MigrationVersion;

/**
 * Migration implementors that also implement this interface will be able to specify their version and description
 * manually, instead of having it automatically computed.
 */
public interface MigrationInfoProvider {
    /**
     * Returns the version after the migration is complete.
     *
     * @return The version after the migration is complete. Never {@code null}.
     */
    MigrationVersion getVersion();

    /**
     * Returns the description for the migration history.
     *
     * @return The description for the migration history. Never {@code null}.
     */
    String getDescription();
}
