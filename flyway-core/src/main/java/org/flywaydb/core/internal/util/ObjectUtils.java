/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util;

/**
 * Collection of utility methods for dealing with objects.
 */
public class ObjectUtils {
    /**
     * Determine if the given objects are equal, returning {@code true}
     * if both are {@code null} or {@code false} if only one is
     * {@code null}.
     *
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     */
    @SuppressWarnings("SimplifiableIfStatement")
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
