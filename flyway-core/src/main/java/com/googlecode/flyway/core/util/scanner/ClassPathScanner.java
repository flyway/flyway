/**
 * Copyright (C) 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.util.scanner;

import com.googlecode.flyway.core.util.ClassPathResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * ClassPath scanner.
 */
public class ClassPathScanner {
    private static final Log LOG = LogFactory.getLog(ClassPathScanner.class);

    /**
     * The registered location scanners.
     */
    private static final List<LocationScanner> LOCATION_SCANNERS = new ArrayList<LocationScanner>();

    static {
        LOCATION_SCANNERS.add(new JarFileLocationScanner());
        LOCATION_SCANNERS.add(new FileSystemLocationScanner());
    }

    /**
     * Scans the classpath for resources under the specified location, starting with the specified prefix and ending with
     * the specified suffix.
     *
     * @param location The location (directory) in the classpath to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffix   The suffix of the resource names to match.
     * @return The resources that were found.
     * @throws IOException when the location could not be scanned.
     */
    public ClassPathResource[] scanForResources(String location, String prefix, String suffix) throws IOException {
        Set<ClassPathResource> classPathResources = new TreeSet<ClassPathResource>();

        Set<String> resourceNames = findResourceNames(location, prefix, suffix);
        for (String resourceName : resourceNames) {
            classPathResources.add(new ClassPathResource(resourceName));
            LOG.debug("Found resource: " + resourceName);
        }

        return classPathResources.toArray(new ClassPathResource[classPathResources.size()]);
    }

    /**
     * Scans the classpath for classes under the specified package implementing any of these interfaces.
     *
     * @param location              The location (package) in the classpath to start scanning.
     *                              Subpackages are also scanned.
     * @param implementedInterfaces The interfaces the matching classes should implement..
     * @return The classes that were found.
     * @throws Exception when the location could not be scanned.
     */
    public Class<?>[] scanForClasses(String location, Class<?>... implementedInterfaces) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        Set<String> resourceNames = findResourceNames(location, "", ".class");
        for (String resourceName : resourceNames) {
            String className = toClassName(resourceName);
            Class<?> clazz = getClassLoader().loadClass(className);
            if (implementedInterfaces.length == 0) {
                classes.add(clazz);
            } else {
                for (Class<?> implementedInterface : implementedInterfaces) {
                    if (implementedInterface.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                }
            }
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    /**
     * Converts this resource name to a fully qualified class name.
     *
     * @param resourceName The resource name.
     * @return The class name.
     */
    private String toClassName(String resourceName) {
        String nameWithDots = resourceName.replace("/", ".");
        return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }

    /**
     * Finds the resources names present at this location and below on the classpath starting with this prefix and
     * ending with this suffix.
     *
     * @param location The location on the classpath to scan.
     * @param prefix   The filename prefix to match.
     * @param suffix   The filename suffix to match.
     * @return The resource names.
     * @throws IOException when scanning this location failed.
     */
    private Set<String> findResourceNames(String location, String prefix, String suffix) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

        String normalizedLocation = normalizeLocation(location);

        Enumeration<URL> locationsUrls = getClassLoader().getResources(normalizedLocation);
        if (!locationsUrls.hasMoreElements()) {
            LOG.debug("Unable to determine URL for classpath location: " + normalizedLocation + " (ClassLoader: " + getClassLoader() + ")");
        }
        while (locationsUrls.hasMoreElements()) {
            URL locationUrl = locationsUrls.nextElement();
            LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

            String scanRoot = URLDecoder.decode(locationUrl.getFile(), "UTF-8");
            if (scanRoot.endsWith("/")) {
                scanRoot = scanRoot.substring(0, scanRoot.length() - 1);
            }

            boolean accepted = false;
            for (LocationScanner locationScanner : LOCATION_SCANNERS) {
                if (locationScanner.acceptUrlProtocol(locationUrl.getProtocol())) {
                    accepted = true;
                    resourceNames.addAll(locationScanner.findResourceNames(normalizedLocation, scanRoot));
                }
            }

            if (!accepted) {
                LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + locationUrl.getProtocol() + ")");
            }
        }

        return filterResourceNames(resourceNames, prefix, suffix);
    }

    /**
     * Normalizes this classpath location by
     * <ul>
     * <li>eliminating all leading and trailing slashes</li>
     * <li>turning all separators into slashes</li>
     * </ul>
     *
     * @param location The location to normalize.
     * @return The normalized location.
     */
    private String normalizeLocation(String location) {
        String directory = location.replace(".", "/").replace("\\", "/");
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        if (directory.endsWith("/")) {
            directory = directory.substring(0, directory.length() - 1);
        }
        return directory;
    }

    /**
     * @return The classloader to use to scan the classpath.
     */
    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Filters this list of resource names to only include the ones whose filename matches this prefix and this suffix.
     *
     * @param resourceNames The names to filter.
     * @param prefix        The prefix to match.
     * @param suffix        The suffix to match.
     * @return The filtered names set.
     */
    private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String suffix) {
        Set<String> filteredResourceNames = new TreeSet<String>();
        for (String resourceName : resourceNames) {
            String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            if (fileName.startsWith(prefix) && fileName.endsWith(suffix)
                    && (fileName.length() > (prefix + suffix).length())) {
                filteredResourceNames.add(resourceName);
            } else {
                LOG.debug("Filtering out resource: " + resourceName + " (filename: " + fileName + ")");
            }
        }
        return filteredResourceNames;
    }
}
