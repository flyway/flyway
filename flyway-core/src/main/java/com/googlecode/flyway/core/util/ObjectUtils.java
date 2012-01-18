package com.googlecode.flyway.core.util;

import java.util.Arrays;

/**
 * Collection of utility methods for dealing with objects.
 */
public class ObjectUtils {
    /**
     * Determine if the given objects are equal, returning <code>true</code>
     * if both are <code>null</code> or <code>false</code> if only one is
     * <code>null</code>.
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
}
