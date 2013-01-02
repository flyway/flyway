/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.SchemaVersion;

/**
 * JavaMigration implementors that also implement this interface will be able to specify their version and description
 * manually, instead of having it automatically computed from the class name.
 *
 * @deprecated Superseeded by com.googlecode.flyway.core.api.migration.MigrationInfoProvider
 */
@Deprecated
public interface JavaMigrationInfoProvider {
    /**
     * @return The schema version after the migration is complete.
     */
    SchemaVersion getVersion();

    /**
     * @return The description for the migration history.
     */
    String getDescription();
}
