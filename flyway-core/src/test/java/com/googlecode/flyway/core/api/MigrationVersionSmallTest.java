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
package com.googlecode.flyway.core.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests MigrationVersion.
 */
public class MigrationVersionSmallTest {
    @Test
    public void compareTo() {
        MigrationVersion v1 = new MigrationVersion("1");
        MigrationVersion v10 = new MigrationVersion("1.0");
        MigrationVersion v11 = new MigrationVersion("1.1");
        MigrationVersion v1100 = new MigrationVersion("1.1.0.0");
        MigrationVersion v1101 = new MigrationVersion("1.1.0.1");
        MigrationVersion v2 = new MigrationVersion("2");
        MigrationVersion v201004171859 = new MigrationVersion("201004171859");
        MigrationVersion v201004180000 = new MigrationVersion("201004180000");

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
        final MigrationVersion a1 = new MigrationVersion("1.2.3.3");
        final MigrationVersion a2 = new MigrationVersion("1.2.3.3");
        assertTrue(a1.compareTo(a2) == 0);
    }

    @Test
    public void testNumber() {
        final MigrationVersion a1 = new MigrationVersion("1.2.13.3");
        final MigrationVersion a2 = new MigrationVersion("1.2.3.3");
        assertTrue(a1.compareTo(a2) > 0);
    }

    @Test
    public void testLength1() {
        final MigrationVersion a1 = new MigrationVersion("1.2.1.3");
        final MigrationVersion a2 = new MigrationVersion("1.2.1");
        assertTrue(a1.compareTo(a2) > 0);
    }

    @Test
    public void testLength2() {
        final MigrationVersion a1 = new MigrationVersion("1.2.1");
        final MigrationVersion a2 = new MigrationVersion("1.2.1.1");
        assertTrue(a1.compareTo(a2) < 0);
    }

    @Test
    public void leadingZeroes() {
        final MigrationVersion v1 = new MigrationVersion("1.0");
        final MigrationVersion v2 = new MigrationVersion("001.0");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
    }

    @Test
    public void trailingZeroes() {
        final MigrationVersion v1 = new MigrationVersion("1.00");
        final MigrationVersion v2 = new MigrationVersion("1");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
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
        final MigrationVersion v1 = new MigrationVersion("0.0");
        final MigrationVersion v2 = new MigrationVersion("0");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v1.equals(v2));
    }

    @Test(expected = FlywayException.class)
    public void missingNumber() {
        new MigrationVersion("1..1");
    }

    @Test(expected = FlywayException.class)
    public void dot() {
        new MigrationVersion(".");
    }

    @Test(expected = FlywayException.class)
    public void startDot() {
        new MigrationVersion(".1");
    }

    @Test(expected = FlywayException.class)
    public void endDot() {
        new MigrationVersion("1.");
    }

    @Test(expected = FlywayException.class)
    public void letters() {
        new MigrationVersion("abc1.0");
    }

    @Test(expected = FlywayException.class)
    public void dash() {
        new MigrationVersion("1.2.1-3");
    }

    @Test(expected = FlywayException.class)
    public void alphaNumeric() {
        new MigrationVersion("1.2.1a-3");
    }
}

