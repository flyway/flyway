/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.exception.FlywayException;
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
    @Test
    public void extractSchemaVersionNoDescription() {
        MigrationVersion version = MigrationInfoHelper.extractVersion("9_4");
        String description = MigrationInfoHelper.extractDescription("9_4");
        assertEquals("9.4", version.toString());
        assertNull(description);
    }

    /**
     * Tests a schema version that includes a description.
     */
    @Test
    public void extractSchemaVersionWithDescription() {
        MigrationVersion version = MigrationInfoHelper.extractVersion("9_4__EmailAxel");
        String description = MigrationInfoHelper.extractDescription("9_4__EmailAxel");
        assertEquals("9.4", version.toString());
        assertEquals("EmailAxel", description);
    }

    /**
     * Tests a schema version that includes a description with spaces.
     */
    @Test
    public void extractSchemaVersionWithDescriptionWithSpaces() {
        MigrationVersion schemaVersion = MigrationInfoHelper.extractVersion("9_4__Big_jump");
        String description = MigrationInfoHelper.extractDescription("9_4__Big_jump");
        assertEquals("9.4", schemaVersion.toString());
        assertEquals("Big jump", description);
    }

    /**
     * Tests a schema version that includes a version with leading zeroes.
     */
    @Test
    public void extractSchemaVersionWithLeadingZeroes() {
        MigrationVersion schemaVersion = MigrationInfoHelper.extractVersion("009_4__EmailAxel");
        String description = MigrationInfoHelper.extractDescription("009_4__EmailAxel");
        assertEquals("009.4", schemaVersion.toString());
        assertEquals("EmailAxel", description);
    }

    @Test(expected = FlywayException.class)
    public void extractSchemaVersionWithLeadingUnderscore() {
        MigrationInfoHelper.extractVersion("_8_0");
    }
}
