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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.Triplet;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for MigrationInfoHelper.
 */
public class MigrationInfoHelperSmallTest {
    /**
     * Tests a schema version that lacks a description.
     */
    @Test(expected = FlywayException.class)
    public void extractSchemaVersionNoDescription() {
        MigrationInfoHelper.extractVersionAndOptionalAndDescription("9_4", "", "__", "");
    }

    @Test
    public void repeatableMigration() {
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("R__EmailAxel.sql", "R", "__", ".sql");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertNull(version);
        assertEquals("EmailAxel", description);
    }

    @Test
    public void extractSchemaVersionDefaults() {
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("V9_4__EmailAxel.sql", "V", "__", ".sql");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("9.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    @Test
    public void extractSchemaVersionCustomSeparator() {
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("V9_4-EmailAxel.sql", "V", "-", ".sql");
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
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("9_4__EmailAxel", "", "__", "");
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
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("9_4__Big_jump", "", "__", "");
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
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("009_4__EmailAxel", "", "__", "");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("009.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscore() {
        MigrationInfoHelper.extractVersionAndOptionalAndDescription("_8_0__Description", "", "__", "");
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscoreAndPrefix() {
        MigrationInfoHelper.extractVersionAndOptionalAndDescription("V_8_0__Description.sql", "V", "__", ".sql");
    }

    @Test
    public void extractSchemaVersionWithVUnderscorePrefix() {
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("V_8_0__Description.sql", "V_", "__", ".sql");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("8.0", version.toString());
        assertEquals("Description", description);
    }

    @Test
    public void extractSchemaVersionWithOptional(){
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("V_8_0__optional__Description.sql", "V_", "__", ".sql");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("8.0", version.toString());
        assertEquals("Description", description);
        assertTrue(info.getCenter());
    }

    @Test
    public void extractSchemaVersionWithRequired(){
        Triplet<MigrationVersion, Boolean, String> info = MigrationInfoHelper.extractVersionAndOptionalAndDescription("V_8_0__required__Description.sql", "V_", "__", ".sql");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("8.0", version.toString());
        assertEquals("Description", description);
        assertFalse(info.getCenter());
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithIncorrectOptionalToken() {
        MigrationInfoHelper.extractVersionAndOptionalAndDescription("V_8_0__toto__Description.sql", "V", "__", ".sql");
    }

}
