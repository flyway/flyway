package org.flywaydb.core.api.migration.baseline;

import lombok.NoArgsConstructor;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.schemahistory.BaseAppliedMigration;

import java.util.Arrays;
import java.util.Date;

@NoArgsConstructor
public class BaselineAppliedMigration extends BaseAppliedMigration {
    public BaselineAppliedMigration(int installedRank,
                                    MigrationVersion version,
                                    String description,
                                    String type,
                                    String script,
                                    Integer checksum,
                                    Date installedOn,
                                    String installedBy,
                                    int executionTime,
                                    boolean success) {
        this.installedRank = installedRank;
        this.version = version;
        this.description = description;
        this.type = BaselineMigrationType.fromString(type);
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.success = success;
    }

    @Override
    public boolean handlesType(String type) {
        return Arrays.stream(BaselineMigrationType.values())
                .map(Enum::toString)
                .anyMatch(t -> t.equalsIgnoreCase(type));
    }

    @Override
    public AppliedMigration create(int installedRank,
                                   MigrationVersion version,
                                   String description,
                                   String type,
                                   String script,
                                   Integer checksum,
                                   Date installedOn,
                                   String installedBy,
                                   int executionTime,
                                   boolean success) {
        return new BaselineAppliedMigration(installedRank, version, description, type, script, checksum, installedOn, installedBy, executionTime, success);
    }

    @Override
    public MigrationState getState(MigrationInfoContext context, boolean outOfOrder, ResolvedMigration resolvedMigration) {
        MigrationState migrationState = super.getState(context, outOfOrder, resolvedMigration);
        MigrationState missingState = super.getMissingState(context, resolvedMigration);
        if (migrationState == MigrationState.BASELINE) {
            if (!isSuccess()) {
                return MigrationState.FAILED;
            }
            if (missingState != null) {
                return missingState;
            }
        }
        return migrationState;
    }
}