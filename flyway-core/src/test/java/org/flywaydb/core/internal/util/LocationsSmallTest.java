/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Small Test for Locations.
 */
public class LocationsSmallTest {
    @Test
    public void mergeLocations() {
        Locations locations = new Locations("db/locations", "db/files", "db/classes");
        List<Location> locationList = locations.getLocations();
        assertEquals(3, locationList.size());
        Iterator<Location> iterator = locationList.iterator();
        assertEquals("db/classes", iterator.next().getPath());
        assertEquals("db/files", iterator.next().getPath());
        assertEquals("db/locations", iterator.next().getPath());
    }

    @Test
    public void mergeLocationsDuplicate() {
        Locations locations = new Locations("db/locations", "db/migration", "db/migration");
        List<Location> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        Iterator<Location> iterator = locationList.iterator();
        assertEquals("db/locations", iterator.next().getPath());
        assertEquals("db/migration", iterator.next().getPath());
    }

    @Test
    public void mergeLocationsOverlap() {
        Locations locations = new Locations("db/migration/oracle", "db/migration", "db/migration");
        List<Location> locationList = locations.getLocations();
        assertEquals(1, locationList.size());
        assertEquals("db/migration", locationList.get(0).getPath());
    }

    @Test
    public void mergeLocationsSimilarButNoOverlap() {
        Locations locations = new Locations("db/migration/oracle", "db/migration", "db/migrationtest");
        List<Location> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        assertTrue(locationList.contains(new Location("db/migration")));
        assertTrue(locationList.contains(new Location("db/migrationtest")));
    }

    @Test
    public void mergeLocationsSimilarButNoOverlapCamelCase() {
        Locations locations = new Locations("/com/xxx/Star/", "/com/xxx/StarTrack/");
        List<Location> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        assertTrue(locationList.contains(new Location("com/xxx/Star")));
        assertTrue(locationList.contains(new Location("com/xxx/StarTrack")));
    }

    @Test
    public void mergeLocationsSimilarButNoOverlapHyphen() {
        Locations locations = new Locations("db/migration/oracle", "db/migration", "db/migration-test");
        List<Location> locationList = locations.getLocations();
        assertEquals(2, locationList.size());
        assertTrue(locationList.contains(new Location("db/migration")));
        assertTrue(locationList.contains(new Location("db/migration-test")));
    }
}
