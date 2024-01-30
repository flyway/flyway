package org.flywaydb.core.internal.scanner.classpath;

import java.net.URL;
import java.util.Set;

/**
 * Scans for classpath resources in this location.
 */
public interface ClassPathLocationScanner {
    /**
     * Finds the resource names below this location on the classpath under this locationUrl.
     *
     * @param location The system-independent location on the classpath.
     * @param locationUrl The system-specific physical location URL.
     * @return The system-independent names of the resources on the classpath.
     */
    Set<String> findResourceNames(String location, URL locationUrl);
}