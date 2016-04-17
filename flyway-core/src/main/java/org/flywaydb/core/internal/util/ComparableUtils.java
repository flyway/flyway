package org.flywaydb.core.internal.util;

public class ComparableUtils {
    /**
     * Compares the specified pair of {@link Comparable} instances, any of those could be {@code null},
     * those nulls are sorted after not-null values.
     */
    public static <T extends Comparable<T>> int compareNullsLast(T a, T b) {
        if (a != null) {
            return b == null ? -1 : a.compareTo(b);
        } else {
            return b == null ? 0 : 1;
        }
    }
}
