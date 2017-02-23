/*
 * Copyright 2010-2017 Boxfuse GmbH
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
        ResolvedMigration b = createResolvedMigration(null, "B");
        MigrationInfoContext context = new MigrationInfoContext();
        context.target = MigrationVersion.LATEST;
        context.latestRepeatableRuns.put("A", 10);
        context.latestRepeatableRuns.put("B", 7);
        context.latestRepeatableRuns.put("C", 5);

        MigrationInfoImpl r1 = new MigrationInfoImpl(createResolvedMigration(null, "C"), createAppliedMigration(5, null, "C"), context, false);
        MigrationInfoImpl v2 = new MigrationInfoImpl(createResolvedMigration("1", "V1"), createAppliedMigration(6, "1", "V1"), context, false);
        MigrationInfoImpl r3 = new MigrationInfoImpl(b, createAppliedMigration(7, null, "B", 123), context, false);
        MigrationInfoImpl r4 = new MigrationInfoImpl(createResolvedMigration(null, "A"), createAppliedMigration(10, null, "A"), context, false);
        MigrationInfoImpl r6 = new MigrationInfoImpl(b, null, context, false);
        MigrationInfoImpl v5 = new MigrationInfoImpl(createResolvedMigration("6", "V2"), null, context, false);

        assertTrue(r1.compareTo(r1) == 0);
        assertTrue(r1.compareTo(v2) < 0);
        assertTrue(r1.compareTo(r3) < 0);
        assertTrue(r1.compareTo(r4) < 0);
        assertTrue(r1.compareTo(v5) < 0);
        assertTrue(r1.compareTo(r6) < 0);

        assertTrue(v2.compareTo(r1) > 0);
        assertTrue(v2.compareTo(v2) == 0);
        assertTrue(v2.compareTo(r3) < 0);
        assertTrue(v2.compareTo(r4) < 0);
        assertTrue(v2.compareTo(v5) < 0);
        assertTrue(v2.compareTo(r6) < 0);

        assertTrue(r3.compareTo(r1) > 0);
        assertTrue(r3.compareTo(v2) > 0);
        assertTrue(r3.compareTo(r3) == 0);
        assertTrue(r3.compareTo(r4) < 0);
        assertTrue(r3.compareTo(v5) < 0);
        assertTrue(r3.compareTo(r6) < 0);

        assertTrue(r4.compareTo(r1) > 0);
        assertTrue(r4.compareTo(v2) > 0);
        assertTrue(r4.compareTo(r3) > 0);
        assertTrue(r4.compareTo(r4) == 0);
        assertTrue(r4.compareTo(v5) < 0);
        assertTrue(r4.compareTo(r6) < 0);

        assertTrue(v5.compareTo(r1) > 0);
        assertTrue(v5.compareTo(v2) > 0);
        assertTrue(v5.compareTo(r3) > 0);
        assertTrue(v5.compareTo(r4) > 0);
        assertTrue(v5.compareTo(v5) == 0);
        assertTrue(v5.compareTo(r6) < 0);

        assertTrue(r6.compareTo(r1) > 0);
        assertTrue(r6.compareTo(v2) > 0);
        assertTrue(r6.compareTo(r3) > 0);
        assertTrue(r6.compareTo(r4) > 0);
        assertTrue(r6.compareTo(v5) > 0);
        assertTrue(r6.compareTo(r6) == 0);
    }

    /**
     * Creates a new resolved repeatable migration with this description.
     *
     * @param description The description of the migration.
     * @return The resolved migration.
     */
    private ResolvedMigration createResolvedMigration(String version, String description) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion(version == null ? null : MigrationVersion.fromVersion(version));
        migration.setDescription(description);
        migration.setScript(description);
        migration.setType(MigrationType.SQL);
        return migration;
    }

    /**
     * Creates a new applied repeatable migration with this description and installed rank.
     *
     * @param installedRank The installed rank of the migration.
     * @param version       The version of the migration.
     * @param description   The description of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedMigration(int installedRank, String version, String description) {
        return createAppliedMigration(installedRank, version, description, null);
    }

    /**
     * Creates a new applied repeatable migration with this description and installed rank.
     *
     * @param installedRank The installed rank of the migration.
     * @param version       The version of the migration.
     * @param description   The description of the migration.
     * @param checksum      The checksum of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedMigration(int installedRank, String version, String description, Integer checksum) {
        return new AppliedMigration(installedRank, version == null ? null : MigrationVersion.fromVersion(version),
                description, MigrationType.SQL, "x", checksum, new Date(), "sa", 123, true);
    }
}
