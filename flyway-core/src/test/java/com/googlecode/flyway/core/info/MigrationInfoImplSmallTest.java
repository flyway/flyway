package com.googlecode.flyway.core.info;

import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.AppliedMigration;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
import org.junit.Test;
import static org.junit.Assert.*;

public class MigrationInfoImplSmallTest {
    @Test
    public void validate() {
        MigrationVersion version = new MigrationVersion("1");
        String description = "test";
        MigrationType type = MigrationType.SQL;

        ResolvedMigration resolvedMigration = new ResolvedMigration();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setType(type);
        resolvedMigration.setChecksum(456);

        AppliedMigration appliedMigration = new AppliedMigration(version, description, type, null, 123, 0, true);

        MigrationInfoImpl migrationInfo =
                new MigrationInfoImpl(resolvedMigration, appliedMigration, new MigrationInfoContext());
        String message = migrationInfo.validate();

        assertTrue(message.contains("123"));
        assertTrue(message.contains("456"));
    }
}
