/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.api;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

/**
 * The state of a migration.
 */
@RequiredArgsConstructor
@Getter
public enum MigrationState {
    /**
     * This migration has not been applied yet.
     */
    PENDING("Pending", true, false, false),
    /**
     * This migration has not been applied yet, and won't be applied because target is set to a lower version.
     */
    ABOVE_TARGET("Above Target", true, false, false),
    /**
     * This migration was not applied against this DB, because the schema history table was baselined with a higher version.
     */
    BELOW_BASELINE("Below Baseline", true, false, false),
    /**
     * This migration has baselined this DB.
     */
    BASELINE("Baseline", true, true, false),
    /**
     * When using cherryPick, this indicates a migration that was not in the cherry picked list.
     * When not using cherryPick, this usually indicates a problem.
     *
     * This migration was not applied against this DB, because a migration with a higher version has already been
     * applied. This probably means some checkins happened out of order.
     *
     * Fix by increasing the version number, run clean and migrate again or rerun migration with outOfOrder enabled.
     */
    IGNORED("Ignored", true, false, false),
    /**
     * This migration succeeded.
     *
     * This migration was applied against this DB, but it is not available locally.
     * This usually results from multiple older migration files being consolidated into a single one.
     *
     */
    MISSING_SUCCESS("Missing", false, true, false),
    /**
     * This migration failed.
     *
     * This migration was applied against this DB, but it is not available locally.
     * This usually results from multiple older migration files being consolidated into a single one.
     *
     * This should rarely, if ever, occur in practice.
     */
    MISSING_FAILED("Failed (Missing)", false, true, true),
    /**
     * This migration succeeded.
     */
    SUCCESS("Success", true, true, false),
    /**
     * This versioned migration succeeded, but has since been undone.
     */
    UNDONE("Undone", true, true, false),
    /**
     * This undo migration is ready to be applied if desired.
     */
    AVAILABLE("Available", true, false, false),
    /**
     * This migration failed.
     */
    FAILED("Failed", true, true, true),
    /**
     * This migration succeeded.
     *
     * This migration succeeded, but it was applied out of order.
     * Rerunning the entire migration history might produce different results!
     *
     */
    OUT_OF_ORDER("Out of Order", true, true, false),
    /**
     * This migration succeeded.
     *
     * This migration has been applied against the DB, but it is not available locally.
     * Its version is higher than the highest version available locally.
     * It was most likely successfully installed by a future version of this deployable.
     *
     */
    FUTURE_SUCCESS("Future", false, true, false),
    /**
     * This migration failed.
     *
     * This migration has been applied against the DB, but it is not available locally.
     * Its version is higher than the highest version available locally.
     * It most likely failed during the installation of a future version of this deployable.
     *
     */
    FUTURE_FAILED("Failed (Future)", false, true, true),
    /**
     * This is a repeatable migration that is outdated and should be re-applied.
     */
    OUTDATED("Outdated", true, true, false),
    /**
     * This is a repeatable migration that is outdated and has already been superseded by a newer run.
     */
    SUPERSEDED("Superseded", true, true, false),
    /**
     * This is a migration that has been marked as deleted.
     */
    DELETED("Deleted", false, true, false);

    private final String displayName;
    private final boolean resolved;
    private final boolean applied;
    private final boolean failed;
}