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
package org.flywaydb.core.internal.scanner.filesystem;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * FileSystem scanner.
 */
public class FileSystemScanner {
    private static final Log LOG = LogFactory.getLog(FileSystemScanner.class);
    private final Charset encoding;





    /**
     * Creates a new filesystem scanner.
     *
     * @param encoding The encoding to use.



     */
    public FileSystemScanner(Charset encoding



    ) {
        this.encoding = encoding;



    }

    /**
     * Scans the FileSystem for resources under the specified location, starting with the specified prefix and ending with
     * the specified suffix.
     *
     * @param location The location in the filesystem to start searching. Subdirectories are also searched.
     * @return The resources that were found.
     */
    public Collection<LoadableResource> scanForResources(Location location) {
        String path = location.getPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "'");

        File dir = new File(path);
        if (!dir.exists()) {
            LOG.warn("Skipping filesystem location:" + path + " (not found)");
            return Collections.emptyList();
        }
        if (!dir.canRead()) {
            LOG.warn("Skipping filesystem location:" + path + " (not readable)");
            return Collections.emptyList();
        }
        if (!dir.isDirectory()) {
            LOG.warn("Skipping filesystem location:" + path + " (not a directory)");
            return Collections.emptyList();
        }

        Set<LoadableResource> resources = new TreeSet<>();

        for (String resourceName : findResourceNamesFromFileSystem(path, new File(path))) {
            resources.add(new FileSystemResource(location, resourceName, encoding



            ));
            LOG.debug("Found filesystem resource: " + resourceName);
        }

        return resources;
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
                    if (file.isHidden()) {
                        // #1807: Skip hidden directories to avoid issues with Kubernetes
                        LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
                    } else {
                        resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation, file));
                    }
                } else {
                    resourceNames.add(file.getPath());
                }
            }
        }

        return resourceNames;
    }
}