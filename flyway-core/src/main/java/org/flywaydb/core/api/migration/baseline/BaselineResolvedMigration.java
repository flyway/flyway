package org.flywaydb.core.api.migration.baseline;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;

public class BaselineResolvedMigration extends ResolvedMigrationImpl {
    public BaselineResolvedMigration(MigrationVersion version,
                                     String description,
                                     String script,
                                     Integer checksum,
                                     Integer equivalentChecksum,
                                     String physicalLocation,
                                     MigrationExecutor executor,
                                     Configuration config) {
        this(version, description, script, checksum, equivalentChecksum, BaselineMigrationType.SQL_BASELINE, physicalLocation, executor, config);
    }

    public BaselineResolvedMigration(MigrationVersion version,
                                     String description,
                                     String script,
                                     Integer checksum,
                                     Integer equivalentChecksum,
                                     MigrationType migrationType,
                                     String physicalLocation,
                                     MigrationExecutor executor,
                                     Configuration config) {
        super(version, description, script, checksum, equivalentChecksum, migrationType, physicalLocation, executor);
    }

    @Override
    public boolean canCompareWith(ResolvedMigration o) {
        return o instanceof BaselineResolvedMigration;
    }

    @Override
    public MigrationState getState(MigrationInfoContext context) {
        MigrationState migrationState = super.getState(context);
        if (migrationState == MigrationState.PENDING && migrationsAppliedOrBaselineExists(context)) {
            return MigrationState.IGNORED;
        }
        if (migrationState == MigrationState.BASELINE_IGNORED && getVersion().equals(context.pendingBaseline)) {
            if (migrationsAppliedOrBaselineExists(context)) {
                return MigrationState.BASELINE_IGNORED;
            }
            return MigrationState.PENDING;
        }
        return migrationState;
    }

    private boolean migrationsAppliedOrBaselineExists(MigrationInfoContext context) {
        return context.appliedBaseline != null || context.lastApplied != MigrationVersion.EMPTY;
    }
}