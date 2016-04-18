package org.flywaydb.core.internal.util;

import org.junit.Test;

import static org.flywaydb.core.internal.util.ComparableUtils.compareNullsLast;
import static org.junit.Assert.assertTrue;

public class ComparableUtilsTest {
    @Test
    public void notNulls() {
        assertTrue(compareNullsLast(2, 2) == 0);
        assertTrue(compareNullsLast(1, 2) < 0);
        assertTrue(compareNullsLast(3, 2) > 0);
    }

    @Test
    public void nulls() {
        assertTrue(compareNullsLast(null, null) == 0);
        assertTrue(compareNullsLast(3, null) < 0);
        assertTrue(compareNullsLast(null, 3) > 0);
    }
}
