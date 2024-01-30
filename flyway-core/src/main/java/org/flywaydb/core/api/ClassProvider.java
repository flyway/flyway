package org.flywaydb.core.api;

import java.util.Collection;

/**
 * A facility to obtain classes.
 */
public interface ClassProvider<I> {
    /**
     * Retrieve all classes which implement the specified interface.
     *
     * @return The non-abstract classes that were found.
     */
    Collection<Class<? extends I>> getClasses();
}