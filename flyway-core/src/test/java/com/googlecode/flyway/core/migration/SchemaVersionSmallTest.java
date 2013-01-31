/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests SchemaVersion.
 */
public class SchemaVersionSmallTest {
    @Test
    public void compareTo() {
        SchemaVersion v1 = new SchemaVersion("1");
        SchemaVersion v10 = new SchemaVersion("1.0");
        SchemaVersion v11 = new SchemaVersion("1.1");
        SchemaVersion v1100 = new SchemaVersion("1.1.0.0");
        SchemaVersion v1101 = new SchemaVersion("1.1.0.1");
        SchemaVersion v2 = new SchemaVersion("2");
        SchemaVersion v201004171859 = new SchemaVersion("201004171859");
        SchemaVersion v201004180000 = new SchemaVersion("201004180000");

        assertTrue(v1.compareTo(v10) == 0);
        assertTrue(v10.compareTo(v1) == 0);
        assertTrue(v1.compareTo(v11) < 0);
        assertTrue(v11.compareTo(v1) > 0);
        assertTrue(v11.compareTo(v1100) == 0);
        assertTrue(v1100.compareTo(v11) == 0);
        assertTrue(v11.compareTo(v1101) < 0);
        assertTrue(v1101.compareTo(v11) > 0);
        assertTrue(v1101.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1101) > 0);
        assertTrue(v201004171859.compareTo(v201004180000) < 0);
        assertTrue(v201004180000.compareTo(v201004171859) > 0);

        assertTrue(v2.compareTo(SchemaVersion.LATEST) < 0);
        assertTrue(SchemaVersion.LATEST.compareTo(v2) > 0);
        assertTrue(v201004180000.compareTo(SchemaVersion.LATEST) < 0);
        assertTrue(SchemaVersion.LATEST.compareTo(v201004180000) > 0);
    }

    @Test
    public void testEquals() {
        final SchemaVersion a1 = new SchemaVersion("1.2.3-3");
        final SchemaVersion a2 = new SchemaVersion("1.2.3.3");
        assertTrue(a1.compareTo(a2) == 0);
    }

    @Test
    public void testNumber() {
        final SchemaVersion a1 = new SchemaVersion("1.2.13-3");
        final SchemaVersion a2 = new SchemaVersion("1.2.3.3");
        assertTrue(a1.compareTo(a2) > 0);
    }

    @Test
    public void testAlphaNumeric() {
        final SchemaVersion a1 = new SchemaVersion("1.2.1a-3");
        final SchemaVersion a2 = new SchemaVersion("1.2.1b.3");
        assertTrue(a1.compareTo(a2) < 0);
    }

    @Test
    public void testLength1() {
        final SchemaVersion a1 = new SchemaVersion("1.2.1-3");
        final SchemaVersion a2 = new SchemaVersion("1.2.1");
        assertTrue(a1.compareTo(a2) > 0);
    }

    @Test
    public void testLength2() {
        final SchemaVersion a1 = new SchemaVersion("1.2.1");
        final SchemaVersion a2 = new SchemaVersion("1.2.1.1");
        assertTrue(a1.compareTo(a2) < 0);
    }

    @Test
    public void leadingZeroes() {
        final SchemaVersion v1 = new SchemaVersion("1.0");
        final SchemaVersion v2 = new SchemaVersion("001.0");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
    }
}

