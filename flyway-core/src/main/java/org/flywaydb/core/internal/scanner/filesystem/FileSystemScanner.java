/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.scanner.filesystem;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;

@CustomLog
public class FileSystemScanner {
    private final Charset defaultEncoding;
    private final boolean detectEncoding;
    private final boolean throwOnMissingLocations;
    private final boolean stream;
    private final Configuration config;

    public FileSystemScanner(final Configuration config) {
        this.defaultEncoding = config.getEncoding();
        this.detectEncoding = config.isDetectEncoding();
        this.stream = config.isStream();
        this.throwOnMissingLocations = config.isFailOnMissingLocations();
        this.config = config;
    }

    /**
     * Scans the FileSystem for resources under the specified location, starting with the specified prefix and ending
     * with the specified suffix.
     *
     * @param location The location in the filesystem to start searching. Subdirectories are also searched.
     * @return The resources that were found.
     */
    public Collection<LoadableResource> scanForResources(final Location location) {
        final String path = location.getRootPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "'");

        final File dir = new File(path);

        final DirectoryValidationResult validationResult = getDirectoryValidationResult(dir);

        if (validationResult != DirectoryValidationResult.VALID) {
            if (throwOnMissingLocations) {
                throw new FlywayException("Failed to find filesystem location: "
                    + path
                    + " ("
                    + validationResult
                    + ")");
            }

            LOG.error("Skipping filesystem location: " + path + " (" + validationResult + ")");
            return Collections.emptyList();
        }

        final Collection<LoadableResource> resources = new TreeSet<>();
        for (final String resourceName : findResourceNamesFromFileSystem(path, dir)) {
            if (matchesAnyWildcardRestrictions(location, resourceName)) {
                final var resource = getResource(location, resourceName);
                resources.add(resource);
            }
        }

        return resources;
    }

    public LoadableResource getResource(final Location location, final String resourceName) {
        boolean detectEncodingForThisResource = detectEncoding;
        Charset encoding = defaultEncoding;
        String encodingBlurb = "";
        if (new File(resourceName + ".conf").exists()) {
            final LoadableResource metadataResource = new FileSystemResource(location,
                resourceName + ".conf",
                defaultEncoding,
                false);
            final SqlScriptMetadata metadata = SqlScriptMetadata.fromResource(metadataResource, null, config);
            if (metadata.encoding() != null) {
                encoding = Charset.forName(metadata.encoding());
                detectEncodingForThisResource = false;
                encodingBlurb = " (with overriding encoding " + encoding + ")";
            }
        }
        final LoadableResource resource = new FileSystemResource(location,
            resourceName,
            encoding,
            detectEncodingForThisResource,
            stream);

        LOG.debug("Found filesystem resource: " + resourceName + encodingBlurb);
        return resource;
    }

    private static Boolean matchesAnyWildcardRestrictions(final Location location, final String path) {
        return Optional.ofNullable(location.getPathRegex()).map(x -> x.matcher(path).matches()).orElse(true);
    }

    private DirectoryValidationResult getDirectoryValidationResult(final File directory) {
        if (!directory.exists()) {
            return DirectoryValidationResult.NOT_FOUND;
        }
        if (!directory.canRead()) {
            return DirectoryValidationResult.NOT_READABLE;
        }
        if (!directory.isDirectory()) {
            return DirectoryValidationResult.NOT_A_DIRECTORY;
        }
        return DirectoryValidationResult.VALID;
    }

    private Set<String> findResourceNamesFromFileSystem(final String scanRootLocation, final File folder) {
        final String path = folder.getPath();
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        final Set<String> resourceNames = new TreeSet<>();

        final File[] files = folder.listFiles();

        if (files == null) {
            if (throwOnMissingLocations) {
                throw new FlywayException("Failed to find filesystem location: "
                    + path
                    + " ("
                    + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER
                    + ")");
            }

            LOG.error("Skipping filesystem location: "
                + path
                + " ("
                + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER
                + ")");
            return Collections.emptySet();
        }

        for (final File file : files) {
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
