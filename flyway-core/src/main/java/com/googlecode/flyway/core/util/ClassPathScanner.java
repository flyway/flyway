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
package com.googlecode.flyway.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClassPath scanner.
 */
public class ClassPathScanner {
    private static final Log LOG = LogFactory.getLog(ClassPathScanner.class);

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
     * @param location The location to scan.
     * @param prefix   The prefix to match.
     * @param suffix   The suffix to match.
     * @return The resource names.
     * @throws IOException when scanning this location failed.
     */
    private Set<String> findResourceNames(String location, String prefix, String suffix) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

        String directory = location.replace(".", "/");
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }

        Enumeration<URL> directoryUrls = getClassLoader().getResources(directory);
        if (!directoryUrls.hasMoreElements()) {
            LOG.debug("Unable to determine URL for classpath location: " + directory + " (ClassLoader: " + getClassLoader() + ")");
        }
        while (directoryUrls.hasMoreElements()) {
            URL directoryUrl = directoryUrls.nextElement();
            LOG.debug("Scanning directory: " + directoryUrl.toExternalForm());

            String scanRoot = URLDecoder.decode(directoryUrl.getFile(), "UTF-8");

            if ("jar".equals(directoryUrl.getProtocol())) {
                String jarFileName = scanRoot.substring(("jar:".length() + 1), scanRoot.indexOf("!"));
                resourceNames.addAll(findResourceNamesFromJarFile(jarFileName, directory));
            } else {
                resourceNames.addAll(findResourceNamesFromFileSystem(scanRoot, scanRoot, directory));
            }
        }

        return filterResourceNames(resourceNames, prefix, suffix);
    }

    /**
     * @return The classloader to use to scan the classpath.
     */
    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Finds all the resource names contained in this directory within this jar file.
     *
     * @param jarFileName The name of the jar file.
     * @param directory   The directory to look under.
     * @return The resource names.
     * @throws IOException when reading the jar file failed.
     */
    private Set<String> findResourceNamesFromJarFile(String jarFileName, String directory) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

        JarFile jarFile = new JarFile(jarFileName);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName();
            if (entryName.startsWith(directory)) {
                resourceNames.add(entryName);
            }
        }

        return resourceNames;
    }

    /**
     * Finds all the resource names contained in this file system folder.
     *
     * @param folderName       The folder to look under.
     * @param scanRoot         The root location of the scan on disk.
     * @param scanRootLocation The root location of the scan on the classpath.
     * @return The resource names;
     * @throws IOException when the folder could not be read.
     */
    private Set<String> findResourceNamesFromFileSystem(String folderName, String scanRoot, String scanRootLocation) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

        File folder = new File(folderName);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.canRead()) {
                String path = file.getCanonicalPath();
                if (file.isDirectory()) {
                    resourceNames.addAll(findResourceNamesFromFileSystem(path, scanRoot, scanRootLocation));
                } else {
                    String normalizedPath = path.replace(File.separator, "/");
                    if (!normalizedPath.startsWith("/")) {
                        normalizedPath = "/" + normalizedPath;
                    }
                    String resourceName = scanRootLocation + normalizedPath.substring(scanRoot.length());
                    resourceNames.add(resourceName);
                }
            }
        }

        return resourceNames;
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
            }
        }
        return filteredResourceNames;
    }
}
