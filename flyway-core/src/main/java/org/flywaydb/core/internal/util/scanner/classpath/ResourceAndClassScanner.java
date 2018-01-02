/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util.scanner.classpath;

import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

/**
 * Scanner for both resources and classes.
 */
public interface ResourceAndClassScanner {
    /**
     * Scans the classpath for resources under the specified location, starting with the specified prefix and ending with
     * the specified suffix.
     *
     * @param location The location in the classpath to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffixes The suffixes of the resource names to match.
     * @return The resources that were found.
     * @throws Exception when the location could not be scanned.
     */
    LoadableResource[] scanForResources(Location location, String prefix, String[] suffixes) throws Exception;

    /**
     * Scans the classpath for concrete classes under the specified package implementing this interface.
     * Non-instantiable abstract classes are filtered out.
     *
     * @param location             The location (package) in the classpath to start scanning.
     *                             Subpackages are also scanned.
     * @param implementedInterface The interface the matching classes should implement.
     * @return The non-abstract classes that were found.
     * @throws Exception when the location could not be scanned.
     */
    Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception;
}
