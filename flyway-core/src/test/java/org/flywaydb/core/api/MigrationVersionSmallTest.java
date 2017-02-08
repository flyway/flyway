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
package org.flywaydb.core.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests MigrationVersion.
 */
public class MigrationVersionSmallTest {
    @Test
    public void compareTo() {
        MigrationVersion v1 = MigrationVersion.fromVersion("1");
        MigrationVersion v10 = MigrationVersion.fromVersion("1.0");
        MigrationVersion v11 = MigrationVersion.fromVersion("1.1");
        MigrationVersion v1100 = MigrationVersion.fromVersion("1.1.0.0");
        MigrationVersion v1101 = MigrationVersion.fromVersion("1.1.0.1");
        MigrationVersion v2 = MigrationVersion.fromVersion("2");
        MigrationVersion v201004171859 = MigrationVersion.fromVersion("201004171859");
        MigrationVersion v201004180000 = MigrationVersion.fromVersion("201004180000");

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

        assertTrue(v2.compareTo(MigrationVersion.LATEST) < 0);
        assertTrue(MigrationVersion.LATEST.compareTo(v2) > 0);
        assertTrue(v201004180000.compareTo(MigrationVersion.LATEST) < 0);
        assertTrue(MigrationVersion.LATEST.compareTo(v201004180000) > 0);
    }

    @Test
    public void testEquals() {
        final MigrationVersion a1 = MigrationVersion.fromVersion("1.2.3.3");
        final MigrationVersion a2 = MigrationVersion.fromVersion("1.2.3.3");
        assertTrue(a1.compareTo(a2) == 0);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    public void testNumber() {
        final MigrationVersion a1 = MigrationVersion.fromVersion("1.2.13.3");
        final MigrationVersion a2 = MigrationVersion.fromVersion("1.2.3.3");
        assertTrue(a1.compareTo(a2) > 0);
    }

    @Test
    public void testLength1() {
        final MigrationVersion a1 = MigrationVersion.fromVersion("1.2.1.3");
        final MigrationVersion a2 = MigrationVersion.fromVersion("1.2.1");
        assertTrue(a1.compareTo(a2) > 0);
    }

    @Test
    public void testLength2() {
        final MigrationVersion a1 = MigrationVersion.fromVersion("1.2.1");
        final MigrationVersion a2 = MigrationVersion.fromVersion("1.2.1.1");
        assertTrue(a1.compareTo(a2) < 0);
    }

    @Test
    public void leadingZeroes() {
        final MigrationVersion v1 = MigrationVersion.fromVersion("1.0");
        final MigrationVersion v2 = MigrationVersion.fromVersion("001.0");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void trailingZeroes() {
        final MigrationVersion v1 = MigrationVersion.fromVersion("1.00");
        final MigrationVersion v2 = MigrationVersion.fromVersion("1");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void fromVersionFactory() {
        assertEquals(MigrationVersion.LATEST, MigrationVersion.fromVersion(MigrationVersion.LATEST.getVersion()));
        assertEquals(MigrationVersion.EMPTY, MigrationVersion.fromVersion(MigrationVersion.EMPTY.getVersion()));
        assertEquals("1.2.3", MigrationVersion.fromVersion("1.2.3").getVersion());
    }

    @Test
    public void empty() {
        assertEquals(MigrationVersion.EMPTY, MigrationVersion.EMPTY);
        assertTrue(MigrationVersion.EMPTY.compareTo(MigrationVersion.EMPTY) == 0);
    }


    @Test
    public void latest() {
        assertEquals(MigrationVersion.LATEST, MigrationVersion.LATEST);
        assertTrue(MigrationVersion.LATEST.compareTo(MigrationVersion.LATEST) == 0);
    }

    @Test
    public void zeros() {
        final MigrationVersion v1 = MigrationVersion.fromVersion("0.0");
        final MigrationVersion v2 = MigrationVersion.fromVersion("0");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(expected = FlywayException.class)
    public void missingNumber() {
        MigrationVersion.fromVersion("1..1");
    }

    @Test(expected = FlywayException.class)
    public void dot() {
        MigrationVersion.fromVersion(".");
    }

    @Test(expected = FlywayException.class)
    public void startDot() {
        MigrationVersion.fromVersion(".1");
    }

    @Test(expected = FlywayException.class)
    public void endDot() {
        MigrationVersion.fromVersion("1.");
    }

    @Test(expected = FlywayException.class)
    public void letters() {
        MigrationVersion.fromVersion("abc1.0");
    }

    @Test(expected = FlywayException.class)
    public void dash() {
        MigrationVersion.fromVersion("1.2.1-3");
    }

    @Test(expected = FlywayException.class)
    public void alphaNumeric() {
        MigrationVersion.fromVersion("1.2.1a-3");
    }

    @Test
    public void testWouldOverflowLong() {
        final String raw = "9999999999999999999999999999999999.8888888231231231231231298797298789132.22";
        MigrationVersion longVersions = MigrationVersion.fromVersion(raw);
        assertEquals(raw, longVersions.getVersion());
    }
}

