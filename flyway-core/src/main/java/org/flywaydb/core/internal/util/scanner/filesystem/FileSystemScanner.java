/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * FileSystem scanner.
 */
public class FileSystemScanner {
    private static final Log LOG = LogFactory.getLog(FileSystemScanner.class);

    /**
     * Scans the FileSystem for resources under the specified location, starting with the specified prefix and ending with
     * the specified suffix.
     *
     * @param location The location in the filesystem to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffix   The suffix of the resource names to match.
     * @return The resources that were found.
     * @throws java.io.IOException when the location could not be scanned.
     */
    public Resource[] scanForResources(Location location, String prefix, String suffix) throws IOException {
        String path = location.getPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "' (Prefix: '" + prefix + "', Suffix: '" + suffix + "')");

        File dir = new File(path);
        if (!dir.isDirectory() || !dir.canRead()) {
            LOG.warn("Unable to resolve location filesystem:" + path);
            return new Resource[0];
        }

        Set<Resource> resources = new TreeSet<Resource>();

        Set<String> resourceNames = findResourceNames(path, prefix, suffix);
        for (String resourceName : resourceNames) {
            resources.add(new FileSystemResource(resourceName));
            LOG.debug("Found filesystem resource: " + resourceName);
        }

        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * Finds the resources names present at this location and below on the classpath starting with this prefix and
     * ending with this suffix.
     *
     * @param path   The path on the classpath to scan.
     * @param prefix The filename prefix to match.
     * @param suffix The filename suffix to match.
     * @return The resource names.
     * @throws java.io.IOException when scanning this location failed.
     */
    private Set<String> findResourceNames(String path, String prefix, String suffix) throws IOException {
        Set<String> resourceNames = findResourceNamesFromFileSystem(path, new File(path));
        return filterResourceNames(resourceNames, prefix, suffix);
    }

    /**
     * Finds all the resource names contained in this file system folder.
     *
     * @param scanRootLocation The root location of the scan on disk.
     * @param folder           The folder to look for resources under on disk.
     * @return The resource names;
     * @throws IOException when the folder could not be read.
     */
    @SuppressWarnings("ConstantConditions")
    private Set<String> findResourceNamesFromFileSystem(String scanRootLocation, File folder) throws IOException {
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        Set<String> resourceNames = new TreeSet<String>();

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
     * @param suffix        The suffix to match.
     * @return The filtered names set.
     */
    private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String suffix) {
        Set<String> filteredResourceNames = new TreeSet<String>();
        for (String resourceName : resourceNames) {
            String fileName = resourceName.substring(resourceName.lastIndexOf(File.separator) + 1);
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
