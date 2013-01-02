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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.resolver.CompositeMigrationResolver;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Small Test for Locations.
 */
public class LocationsSmallTest {
    @Test
    public void mergeLocations() {
        Locations locations = new Locations("db/locations", "db/files", "db/classes");
        List<String> locationList = locations.getLocations();
        assertEquals(3, locationList.size());
        Iterator<String> iterator = locationList.iterator();
        assertEquals("db/classes", iterator.next());
        assertEquals("db/files", iterator.next());
        assertEquals("db/locations", iterator.next());
    }

    @Test
    public void mergeLocationsDuplicate() {
        Locations locations = new Locations("db/locations", "db/migration", "db/migration");
        List<String> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        Iterator<String> iterator = locationList.iterator();
        assertEquals("db/locations", iterator.next());
        assertEquals("db/migration", iterator.next());
    }

    @Test
    public void mergeLocationsOverlap() {
        Locations locations = new Locations("db/migration/oracle", "db/migration", "db/migration");
        List<String> locationList = locations.getLocations();
        assertEquals(1, locationList.size());
        assertEquals("db/migration", locationList.get(0));
    }

    @Test
    public void mergeLocationsSimilarButNoOverlap() {
        Locations locations = new Locations("db/migration/oracle", "db/migration", "db/migrationtest");
        List<String> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        assertTrue(locationList.contains("db/migration"));
        assertTrue(locationList.contains("db/migrationtest"));
    }

    @Test
    public void mergeLocationsSimilarButNoOverlapHyphen() {
        Locations locations = new Locations("db/migration/oracle", "db/migration", "db/migration-test");
        List<String> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        assertTrue(locationList.contains("db/migration"));
        assertTrue(locationList.contains("db/migration-test"));
    }
}
