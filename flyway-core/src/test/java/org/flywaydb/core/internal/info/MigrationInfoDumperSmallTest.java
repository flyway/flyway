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

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for MigrationInfoDumper.
 */
public class MigrationInfoDumperSmallTest {
    @Test
    public void dumpEmpty() {
        String table = MigrationInfoDumper.dumpToAsciiTable(new MigrationInfo[0]);
        String[] lines = StringUtils.tokenizeToStringArray(table, "\n");

        assertEquals(5, lines.length);
        for (String line : lines) {
            assertEquals(lines[0].length(), line.length());
        }
    }

    @Test
    public void dump2pending() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration("1"), createAvailableMigration("2.2014.09.11.55.45613")),
                        createMetaDataTable(), MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        String table = MigrationInfoDumper.dumpToAsciiTable(migrationInfoService.all());
        String[] lines = StringUtils.tokenizeToStringArray(table, "\n");

        assertEquals(6, lines.length);
        for (String line : lines) {
            assertEquals(lines[0].length(), line.length());
        }
    }

    /**
     * Creates a new available migration with this version.
     *
     * @param version The version of the migration.
     * @return The available migration.
     */
    private ResolvedMigration createAvailableMigration(String version) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion(MigrationVersion.fromVersion(version));
        migration.setDescription("abc very very very very very very very very very very long");
        migration.setScript("x");
        migration.setType(MigrationType.SQL);
        return migration;
    }

    /**
     * Creates a migrationResolver for testing.
     *
     * @param resolvedMigrations The resolved migrations.
     * @return The migration resolver.
     */
    private MigrationResolver createMigrationResolver(final ResolvedMigration... resolvedMigrations) {
        return new MigrationResolver() {
            public List<ResolvedMigration> resolveMigrations() {
                return Arrays.asList(resolvedMigrations);
            }
        };
    }

    /**
     * Creates a metadata table for testing.
     *
     * @return The metadata table.
     */
    private MetaDataTable createMetaDataTable() {
        MetaDataTable metaDataTable = mock(MetaDataTable.class);
        when(metaDataTable.allAppliedMigrations()).thenReturn(new ArrayList<AppliedMigration>());
        return metaDataTable;
    }
}
