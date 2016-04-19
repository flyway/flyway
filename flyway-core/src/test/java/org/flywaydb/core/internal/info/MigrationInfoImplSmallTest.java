/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertTrue;

public class MigrationInfoImplSmallTest {
    @Test
    public void validate() {
        MigrationVersion version = MigrationVersion.fromVersion("1");
        String description = "test";
        MigrationType type = MigrationType.SQL;

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setType(type);
        resolvedMigration.setChecksum(456);

        AppliedMigration appliedMigration = new AppliedMigration(1, version, description, type, null, 123, new Date(), "abc", 0, true);

        MigrationInfoImpl migrationInfo =
                new MigrationInfoImpl(resolvedMigration, appliedMigration, new MigrationInfoContext(), false);
        String message = migrationInfo.validate();

        assertTrue(message.contains("123"));
        assertTrue(message.contains("456"));
    }

    @Test
    public void validateFuture() {
        MigrationVersion version = MigrationVersion.fromVersion("1");
        String description = "test";
        MigrationType type = MigrationType.SQL;

        AppliedMigration appliedMigration = new AppliedMigration(1, version, description, type, null, 123, new Date(), "abc", 0, true);

        MigrationInfoImpl migrationInfo =
                new MigrationInfoImpl(null, appliedMigration, new MigrationInfoContext(), false);
        String message = migrationInfo.validate();

        assertTrue(message, message.contains("not resolved"));
    }

    @Test
    public void compareToRepeatable() {
        MigrationInfoContext context = new MigrationInfoContext();
        MigrationInfoImpl c = new MigrationInfoImpl(createResolvedMigration("C"), createAppliedMigration(5, "C"), context, false);
        MigrationInfoImpl a = new MigrationInfoImpl(createResolvedMigration("A"), createAppliedMigration(10, "A"), context, false);
        MigrationInfoImpl b = new MigrationInfoImpl(createResolvedMigration("B"), null, context, false);

        assertTrue(a.compareTo(a) == 0);
        assertTrue(b.compareTo(b) == 0);
        assertTrue(c.compareTo(c) == 0);

        assertTrue(c.compareTo(a) < 0);
        assertTrue(a.compareTo(c) > 0);

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        assertTrue(c.compareTo(b) < 0);
        assertTrue(b.compareTo(c) > 0);
    }

    /**
     * Creates a new resolved repeatable migration with this description.
     *
     * @param description The description of the migration.
     * @return The resolved migration.
     */
    private ResolvedMigration createResolvedMigration(String description) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setDescription(description);
        migration.setScript(description);
        migration.setType(MigrationType.SQL);
        return migration;
    }

    /**
     * Creates a new applied repeatable migration with this description and installed rank.
     *
     * @param installedRank     The installed rank of the migration.
     * @param description The description of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedMigration(int installedRank, String description) {
        return new AppliedMigration(installedRank, null, description,
                MigrationType.SQL, "x", null, new Date(), "sa", 123, true);
    }
}
