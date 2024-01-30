package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.api.resource.LoadableResource;

import java.util.Collection;

/**
 * Scanner for both resources and classes.
 */
public interface ResourceAndClassScanner<I> {
    /**
     * Scans the classpath for resources under the configured location.
     *
     * @return The resources that were found.
     */
    Collection<LoadableResource> scanForResources();

    /**
     * Scans the classpath for concrete classes under the specified package implementing the specified interface.
     * Non-instantiable abstract classes are filtered out.
     *
     * @return The non-abstract classes that were found.
     */
    Collection<Class<? extends I>> scanForClasses();
}