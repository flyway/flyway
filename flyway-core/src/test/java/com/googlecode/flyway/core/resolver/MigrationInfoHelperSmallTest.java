/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.resolver;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.util.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for MigrationInfoHelper.
 */
public class MigrationInfoHelperSmallTest {
    /**
     * Tests a schema version that lacks a description.
     */
    @Test(expected = FlywayException.class)
    public void extractSchemaVersionNoDescription() {
        MigrationInfoHelper.extractVersionAndDescription("9_4", "", "");
    }

    /**
     * Tests a schema version that includes a description.
     */
    @Test
    public void extractSchemaVersionWithDescription() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("9_4__EmailAxel", "", "");
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
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("9_4__Big_jump", "", "");
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
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("009_4__EmailAxel", "", "");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("009.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscore() {
        MigrationInfoHelper.extractVersionAndDescription("_8_0__Description", "", "");
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscoreAndPrefix() {
        MigrationInfoHelper.extractVersionAndDescription("V_8_0__Description.sql", "V", ".sql");
    }

    @Test
    public void extractSchemaVersionWithVUnderscorePrefix() {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription("V_8_0__Description.sql", "V_", ".sql");
        MigrationVersion version = info.getLeft();
        String description = info.getRight();
        assertEquals("8.0", version.toString());
        assertEquals("Description", description);
    }
}
