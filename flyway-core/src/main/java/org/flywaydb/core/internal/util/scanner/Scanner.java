/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.scanner;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.classpath.ResourceAndClassScanner;
import org.flywaydb.core.internal.util.scanner.classpath.android.AndroidScanner;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemScanner;

/**
 * Scanner for Resources and Classes.
 */
public class Scanner {
    private final ResourceAndClassScanner resourceAndClassScanner;

    private final ClassLoader classLoader;
    private final FileSystemScanner fileSystemScanner = new FileSystemScanner();

    public Scanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
        if (new FeatureDetector(classLoader).isAndroidAvailable()) {
            resourceAndClassScanner = new AndroidScanner(classLoader);
        } else {
            resourceAndClassScanner = new ClassPathScanner(classLoader);
        }
    }

    /**
     * Scans this location for resources, starting with the specified prefix and ending with the specified suffix.
     *
     * @param location The location to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffixes   The suffixes of the resource names to match.
     * @return The resources that were found.
     */
    public LoadableResource[] scanForResources(Location location, String prefix, String[] suffixes) {
        try {
            if (location.isFileSystem()) {
                return fileSystemScanner.scanForResources(location, prefix, suffixes);
            }
            return resourceAndClassScanner.scanForResources(location, prefix, suffixes);
        } catch (Exception e) {
            throw new FlywayException("Unable to scan for SQL migrations in location: " + location, e);
        }
    }


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
    public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception {
        return resourceAndClassScanner.scanForClasses(location, implementedInterface);
    }

    /**
     * @return The class loader used for scanning.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}