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
package com.googlecode.flyway.core.api;

/**
 * The state of a migration.
 */
public enum MigrationState {
    /**
     * This migration has not been applied yet.
     */
    PENDING("Pending", false),

    /**
     * This migration was not applied against this DB, because the metadata table was initialized with a higher version.
     */
    PREINIT("PreInit", false),

    /**
     * <p>This usually indicates a problem.</p>
     * <p>
     * This migration was not applied against this DB, because a migration with a higher version has already been
     * applied. This probably means some checkins happened out of order.
     * </p>
     * <p>Fix by increasing the version number or clean and migrate again.</p>
     */
    IGNORED("Ignored", false),

    /**
     * <p>This migration succeeded.</p>
     * <p>
     * This migration was applied against this DB, but it is not available locally.
     * This usually results from multiple older migration files being consolidated into a single one.
     * </p>
     */
    MISSING_SUCCESS("Missing", true),

    /**
     * <p>This migration failed.</p>
     * <p>
     * This migration was applied against this DB, but it is not available locally.
     * This usually results from multiple older migration files being consolidated into a single one.
     * </p>
     * <p>This should rarely, if ever, occur in practice.</p>
     */
    MISSING_FAILED("MisFail", true),

    /**
     * This migration succeeded.
     */
    SUCCESS("Success", true),

    /**
     * This migration failed.
     */
    FAILED("Failed", true),

    /**
     * <p>This migration succeeded.</p>
     * <p>
     * This migration has been applied against the DB, but it is not available locally.
     * Its version is higher than the highest version available locally.
     * It was most likely successfully installed by a future version of this deployable.
     * </p>
     */
    FUTURE_SUCCESS("Future", true),

    /**
     * <p>This migration failed.</p>
     * <p>
     * This migration has been applied against the DB, but it is not available locally.
     * Its version is higher than the highest version available locally.
     * It most likely failed during the installation of a future version of this deployable.
     * </p>
     */
    FUTURE_FAILED("FutFail", true);

    /**
     * The name suitable for display to the end-user.
     */
    private final String displayName;

    /**
     * Flag indicating if this migration has been applied or not.
     */
    private final boolean applied;

    /**
     * Creates a new MigrationState.
     *
     * @param displayName The name suitable for display to the end-user.
     * @param applied     Flag indicating if this migration has been applied or not.
     */
    MigrationState(String displayName, boolean applied) {
        this.displayName = displayName;
        this.applied = applied;
    }

    /**
     * @return The name suitable for display to the end-user.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Flag indicating if this migration has been applied or not.
     */
    public boolean isApplied() {
        return applied;
    }
}
