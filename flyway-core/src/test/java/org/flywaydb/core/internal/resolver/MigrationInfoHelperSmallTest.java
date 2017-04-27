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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for MigrationInfoHelper.
 */
public class MigrationInfoHelperSmallTest {
    /**
     * Tests a schema version that lacks a description.
     */
    @Test(expected = FlywayException.class)
    public void extractSchemaVersionNoDescription() {
        MigrationInfoHelper.extractVersionAndDescription("9_4", "", "__", "", false);
    }

    @Test
    public void repeatableMigration() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("R__EmailAxel.sql", "R", "__", ".sql", true);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertNull(version);
        assertEquals("EmailAxel", description);
    }

    @Test(expected = FlywayException.class)
    public void repeatableMigrationVersion() {
        MigrationInfoHelper.extractVersionAndDescription("R1.0__EmailAxel.sql", "R", "__", ".sql", true);
    }

    @Test(expected = FlywayException.class)
    public void versionedMigrationNoVersion() {
        MigrationInfoHelper.extractVersionAndDescription("V__EmailAxel.sql", "V", "__", ".sql", false);
    }

    @Test
    public void extractSchemaVersionDefaults() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("V9_4__EmailAxel.sql", "V", "__", ".sql", false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("9.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    @Test
    public void extractSchemaVersionCustomSeparator() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("V9_4-EmailAxel.sql", "V", "-", ".sql", false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("9.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    /**
     * Tests a schema version that includes a description.
     */
    @Test
    public void extractSchemaVersionWithDescription() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("9_4__EmailAxel", "", "__", "", false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("9.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    /**
     * Tests a schema version that includes a description with spaces.
     */
    @Test
    public void extractSchemaVersionWithDescriptionWithSpaces() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("9_4__Big_jump", "", "__", "", false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("9.4", version.toString());
        assertEquals("Big jump", description);
    }

    /**
     * Tests a schema version that includes a version with leading zeroes.
     */
    @Test
    public void extractSchemaVersionWithLeadingZeroes() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("009_4__EmailAxel", "", "__", "", false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("009.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscore() {
        MigrationInfoHelper.extractVersionAndDescription("_8_0__Description", "", "__", "", false);
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscoreAndPrefix() {
        MigrationInfoHelper.extractVersionAndDescription("V_8_0__Description.sql", "V", "__", ".sql", false);
    }

    @Test
    public void extractSchemaVersionWithVUnderscorePrefix() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("V_8_0__Description.sql", "V_", "__", ".sql", false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("8.0", version.toString());
        assertEquals("Description", description);
    }
}
