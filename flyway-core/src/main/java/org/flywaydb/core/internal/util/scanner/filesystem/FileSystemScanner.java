/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.scanner.filesystem;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * FileSystem scanner.
 */
public class FileSystemScanner {
    private static final Log LOG = LogFactory.getLog(FileSystemScanner.class);
    private final Configuration configuration;

    /**
     * Creates a new filesystem scanner.
     *
     * @param configuration The Flyway configuration.
     */
    public FileSystemScanner(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Scans the FileSystem for resources under the specified location, starting with the specified prefix and ending with
     * the specified suffix.
     *
     * @param location The location in the filesystem to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffixes The suffixes of the resource names to match.
     * @return The resources that were found.
     */
    public LoadableResource[] scanForResources(Location location, String prefix, String... suffixes) {
        String path = location.getPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "' (Prefix: '" + prefix + "', Suffixes: '"
                + StringUtils.arrayToCommaDelimitedString(suffixes) + "')");

        File dir = new File(path);
        if (!dir.exists()) {
            LOG.warn("Skipping filesystem location:" + path + " (not found)");
            return new LoadableResource[0];
        }
        if (!dir.canRead()) {
            LOG.warn("Skipping filesystem location:" + path + " (not readable)");
            return new LoadableResource[0];
        }
        if (!dir.isDirectory()) {
            LOG.warn("Skipping filesystem location:" + path + " (not a directory)");
            return new LoadableResource[0];
        }

        Set<LoadableResource> resources = new TreeSet<>();

        Set<String> resourceNames = findResourceNames(path, prefix, suffixes);
        for (String resourceName : resourceNames) {
            resources.add(new FileSystemResource(resourceName, configuration.getEncoding()



            ));
            LOG.debug("Found filesystem resource: " + resourceName);
        }

        return resources.toArray(new LoadableResource[0]);
    }

    /**
     * Finds the resources names present at this location and below on the classpath starting with this prefix and
     * ending with this suffix.
     *
     * @param path     The path on the classpath to scan.
     * @param prefix   The filename prefix to match.
     * @param suffixes The filename suffixes to match.
     * @return The resource names.
     */
    private Set<String> findResourceNames(String path, String prefix, String[] suffixes) {
        Set<String> resourceNames = findResourceNamesFromFileSystem(path, new File(path));
        return filterResourceNames(resourceNames, prefix, suffixes);
    }

    /**
     * Finds all the resource names contained in this file system folder.
     *
     * @param scanRootLocation The root location of the scan on disk.
     * @param folder           The folder to look for resources under on disk.
     * @return The resource names;
     */
    @SuppressWarnings("ConstantConditions")
    private Set<String> findResourceNamesFromFileSystem(String scanRootLocation, File folder) {
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        Set<String> resourceNames = new TreeSet<>();

        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.canRead()) {
                if (file.isDirectory()) {
                    resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation, file));
                } else {
                    resourceNames.add(file.getPath());
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
     * @param suffixes      The suffixes to match.
     * @return The filtered names set.
     */
    private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String[] suffixes) {
        Set<String> filteredResourceNames = new TreeSet<>();
        for (String resourceName : resourceNames) {
            String fileName = resourceName.substring(resourceName.lastIndexOf(File.separator) + 1);
            if (StringUtils.startsAndEndsWith(fileName, prefix, suffixes)) {
                filteredResourceNames.add(resourceName);
            } else {
                LOG.debug("Filtering out resource: " + resourceName + " (filename: " + fileName + ")");
            }
        }
        return filteredResourceNames;
    }
}