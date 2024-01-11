package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionsUtils {

    /**
     * Checks if the collection is not null and not empty.
     *
     * @param collection The collection to check.
     * @return {@code true} if the collection is not null and not empty, {@code false} otherwise.
     */
    public static boolean hasItems(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}