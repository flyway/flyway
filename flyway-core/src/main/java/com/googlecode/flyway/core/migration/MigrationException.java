/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationVersion;

/**
 * Exception indicating that migration failed.
 */
public class MigrationException extends FlywayException {
    /**
     * The version of the migration that failed.
     */
    private final MigrationVersion version;

    /**
     * Flag indicating whether a rollback was performed or not.
     */
    private final boolean rollback;

    /**
     * Creates a new MigrationException for this version.
     *
     * @param version  The version of the migration that failed.
     * @param rollback Flag indicating whether a rollback was performed or not.
     */
    public MigrationException(MigrationVersion version, boolean rollback) {
        super();
        this.version = version;
        this.rollback = rollback;
    }

    @Override
    public String getMessage() {
        if (rollback) {
            return "Migration to version " + version + " failed! Changes successfully rolled back.";
        }

        return "Migration to version " + version + " failed! Please restore backups and roll back database and code!";
    }
}
