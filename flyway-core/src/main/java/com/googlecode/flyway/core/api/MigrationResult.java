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
package com.googlecode.flyway.core.api;

/**
 * A result of a migration.
 *
 * @author Pavel Boldyrev
 */
public class MigrationResult {

    private final boolean success;
    private final MigrationVersion migrationVersion;
    private final Throwable errorCause;

    /**
     * Creates a MigrationResult.
     *
     * @param success          The migration successful flag.
     * @param migrationVersion The migration version.
     * @param errorCause       An optional error cause for a failed migration.
     */
    private MigrationResult(final boolean success, final MigrationVersion migrationVersion, final Throwable errorCause) {
        this.success = success;
        this.migrationVersion = migrationVersion;
        this.errorCause = errorCause;
    }

    /**
     * Creates a MigrationResult of successful migration.
     *
     * @param migrationVersion The migration version.
     * @return The created MigrationResult.
     */
    public static MigrationResult createSuccess(final MigrationVersion migrationVersion) {
        return new MigrationResult(true, migrationVersion, null);
    }

    /**
     * Creates a MigrationResult of failed migration.
     *
     * @param migrationVersion The migration version.
     * @param cause            An optional error cause.
     * @return The created MigrationResult.
     */
    public static MigrationResult createFailed(final MigrationVersion migrationVersion, final Throwable cause) {
        return new MigrationResult(false, migrationVersion, cause);
    }

    /**
     * @return The success flag.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return The migration version.
     */
    public MigrationVersion getMigrationVersion() {
        return migrationVersion;
    }

    /**
     * @return The error cause for failed migration, can be <b>null</b>!
     */
    public Throwable getErrorCause() {
        return errorCause;
    }
}
