/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.baseline.BaselineMigrationType;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.schemahistory.BaseAppliedMigration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationCompactionTest {
    @Test
    void usesLatestBaselineReachedByDatabase() {
        final List<ResolvedMigration> resolvedMigrations = List.of(baseline("2"), baseline("5"), versioned("6"));

        assertEquals(MigrationVersion.fromVersion("2"),
            MigrationInfoServiceImpl.findCompactionBaseline(resolvedMigrations, MigrationVersion.fromVersion("3")));
        assertEquals(MigrationVersion.fromVersion("5"),
            MigrationInfoServiceImpl.findCompactionBaseline(resolvedMigrations, MigrationVersion.fromVersion("5")));
    }

    @Test
    void doesNotCompactBeforeDatabaseReachesBaseline() {
        final List<ResolvedMigration> resolvedMigrations = List.of(baseline("2"), versioned("3"));

        assertNull(MigrationInfoServiceImpl.findCompactionBaseline(resolvedMigrations, MigrationVersion.fromVersion("1")));
        assertNull(MigrationInfoServiceImpl.findCompactionBaseline(resolvedMigrations, MigrationVersion.EMPTY));
    }

    @Test
    void doesNotUseBaselineConfiguredNotToExecute() {
        final List<ResolvedMigration> resolvedMigrations = List.of(resolvedMigration("2",
            BaselineMigrationType.SQL_BASELINE,
            false));

        assertNull(MigrationInfoServiceImpl.findCompactionBaseline(resolvedMigrations, MigrationVersion.fromVersion("2")));
    }

    @Test
    void resolvedMigrationCoveredByBaselineRemainsSuccessful() {
        final MigrationInfoContext context = contextWithCompactionBaseline("3");
        final BaseAppliedMigration appliedMigration = appliedMigration("2", true);
        final MigrationInfoImpl migrationInfo = new MigrationInfoImpl(versioned("2"),
            appliedMigration,
            context,
            false,
            false,
            false);

        assertSame(MigrationState.SUCCESS, migrationInfo.getState());
    }

    @Test
    void compactedMigrationPassesValidation() {
        final MigrationInfoContext context = contextWithCompactionBaseline("3");
        final BaseAppliedMigration appliedMigration = appliedMigration("2", true);
        final MigrationInfoImpl migrationInfo = new MigrationInfoImpl(null,
            appliedMigration,
            context,
            false,
            false,
            false);

        assertSame(MigrationState.COMPACTED, migrationInfo.getState());
        assertNull(migrationInfo.validate());
    }

    @Test
    void missingMigrationAboveBaselineStillFailsValidation() {
        final MigrationInfoContext context = contextWithCompactionBaseline("2");
        context.lastResolved = MigrationVersion.fromVersion("4");
        final MigrationInfoImpl migrationInfo = new MigrationInfoImpl(null,
            appliedMigration("3", true),
            context,
            false,
            false,
            false);

        assertSame(MigrationState.MISSING_SUCCESS, migrationInfo.getState());
        final ErrorDetails errorDetails = migrationInfo.validate();
        assertSame(CoreErrorCode.APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED, errorDetails.errorCode);
    }

    @Test
    void failedMigrationIsNeverCompacted() {
        final MigrationInfoContext context = contextWithCompactionBaseline("3");
        context.lastResolved = MigrationVersion.fromVersion("4");
        final MigrationInfoImpl migrationInfo = new MigrationInfoImpl(null,
            appliedMigration("2", false),
            context,
            false,
            false,
            false);

        assertSame(MigrationState.MISSING_FAILED, migrationInfo.getState());
    }

    @Test
    void laggingDatabaseIsToldToReachBaselineInsteadOfRepairing() {
        final MigrationInfoContext context = contextWithCompactionBaseline(null);
        context.lastApplied = MigrationVersion.fromVersion("1");
        context.lastResolved = MigrationVersion.fromVersion("3");
        context.pendingBaseline = MigrationVersion.fromVersion("2");
        final MigrationInfoImpl migrationInfo = new MigrationInfoImpl(null,
            appliedMigration("1", true),
            context,
            false,
            false,
            false);

        final ErrorDetails errorDetails = migrationInfo.validate();
        assertTrue(errorDetails.errorMessage.contains("has not reached baseline migration 2"));
        assertTrue(errorDetails.errorMessage.contains("Restore the removed versioned migrations"));
    }

    private MigrationInfoContext contextWithCompactionBaseline(final String version) {
        final MigrationInfoContext context = new MigrationInfoContext(new ClassicConfiguration());
        context.compactionBaseline = version == null ? null : MigrationVersion.fromVersion(version);
        return context;
    }

    private BaseAppliedMigration appliedMigration(final String version, final boolean success) {
        return new BaseAppliedMigration(1,
            MigrationVersion.fromVersion(version),
            "migration " + version,
            CoreMigrationType.SQL.toString(),
            "V" + version + "__migration.sql",
            123,
            null,
            "test",
            1,
            success);
    }

    private ResolvedMigration baseline(final String version) {
        return resolvedMigration(version, BaselineMigrationType.SQL_BASELINE);
    }

    private ResolvedMigration versioned(final String version) {
        return resolvedMigration(version, CoreMigrationType.SQL);
    }

    private ResolvedMigration resolvedMigration(final String version,
        final org.flywaydb.core.extensibility.MigrationType type) {
        return resolvedMigration(version, type, true);
    }

    private ResolvedMigration resolvedMigration(final String version,
        final org.flywaydb.core.extensibility.MigrationType type,
        final boolean shouldExecute) {
        return new ResolvedMigrationImpl(MigrationVersion.fromVersion(version),
            "migration " + version,
            "migration.sql",
            123,
            123,
            type,
            "migration.sql",
            new MigrationExecutor() {
                @Override
                public List<Results> execute(final Context context) {
                    return List.of();
                }

                @Override
                public boolean canExecuteInTransaction() {
                    return true;
                }

                @Override
                public boolean shouldExecute() {
                    return shouldExecute;
                }
            });
    }
}
