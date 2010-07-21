/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import com.googlecode.flyway.core.migration.BaseMigration;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for BaseMigration.
 */
public class BaseMigrationSmallTest {
    /**
     * Tests a schema version that lacks a description.
     */
    @Test
    public void extractSchemaVersionNoDescription() {
        SchemaVersion schemaVersion = BaseMigration.extractSchemaVersion("9_4");
        assertEquals("9.4", schemaVersion.getVersion());
        assertNull(schemaVersion.getDescription());
    }

    /**
     * Tests a schema version that includes a description.
     */
    @Test
    public void extractSchemaVersionWithDescription() {
        SchemaVersion schemaVersion = BaseMigration.extractSchemaVersion("9_4__EmailAxel");
        assertEquals("9.4", schemaVersion.getVersion());
        assertEquals("EmailAxel", schemaVersion.getDescription());
    }

    /**
     * Tests a schema version that includes a description with spaces.
     */
    @Test
    public void extractSchemaVersionWithDescriptionWithSpaces() {
        SchemaVersion schemaVersion = BaseMigration.extractSchemaVersion("9_4__Big_jump");
        assertEquals("9.4", schemaVersion.getVersion());
        assertEquals("Big jump", schemaVersion.getDescription());
    }

    /**
     * Tests a schema version that includes a version with leading zeroes.
     */
    @Test
    public void extractSchemaVersionWithLeadingZeroes() {
        SchemaVersion schemaVersion = BaseMigration.extractSchemaVersion("009_4__EmailAxel");
        assertEquals("9.4", schemaVersion.getVersion());
        assertEquals("EmailAxel", schemaVersion.getDescription());
    }
}
