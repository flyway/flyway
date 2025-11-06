/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import static org.flywaydb.core.api.CoreLocationPrefix.FILESYSTEM_PREFIX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.scanner.ReadWriteLocationHandler;

public class FilesystemLocationHandler implements ReadWriteLocationHandler {

    @Getter
    private final String prefix = FILESYSTEM_PREFIX;

    @Override
    public Collection<LoadableResource> scanForResources(final Location location, final Configuration configuration) {
        final FileSystemScanner fileSystemScanner = new FileSystemScanner(configuration);
        return fileSystemScanner.scanForResources(location);
    }

    @Override
    public Optional<LoadableResource> getResource(final Location location, final Configuration configuration) {
        final Path resolvedPath = Path.of(ConfigUtils.getFilenameWithWorkingDirectory(location.getRootPath(),
            configuration));
        final String parentPath = Optional.ofNullable(resolvedPath.getParent()).map(Path::toString).orElse("");
        final Location parentLocation = Location.fromPath(FILESYSTEM_PREFIX, parentPath);

        final FileSystemScanner fileSystemScanner = new FileSystemScanner(configuration);
        return Files.exists(resolvedPath) ? Optional.of(fileSystemScanner.getResource(parentLocation,
            resolvedPath.toString())) : Optional.empty();
    }

    @Override
    public boolean handlesWildcards() {
        return true;
    }

    @Override
    public String getPathSeparator() {
        return File.separator;
    }

    @Override
    public String normalizePath(final String path) {
        return new File(path).getPath();
    }

    @Override
    public OutputStream getOutputStream(final Location fileLocation, final Configuration configuration) {
        final File file;
        try {
            file = new File(ConfigUtils.getFilenameWithWorkingDirectory(fileLocation.getRootPath(),
                configuration)).getCanonicalFile();
        } catch (final IOException e) {
            throw new FlywayException("Unable to get canonical path for file "
                + fileLocation.getRootPath()
                + ": "
                + e.getMessage(), e, CoreErrorCode.ERROR);
        }
        final String path = file.getAbsolutePath();
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new FlywayException("Unable to write output to " + path + " as it is a directory and not a file",
                    CoreErrorCode.ERROR);
            }
            if (!file.canWrite()) {
                throw new FlywayException("Unable to write output to " + path + " as it is write-protected",
                    CoreErrorCode.ERROR);
            }
        } else {
            final File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new FlywayException("Unable to create parent directories for output to " + path,
                        CoreErrorCode.ERROR);
                }
            }
        }

        try {
            return (new FileOutputStream(path));
        } catch (final FileNotFoundException e) {
            throw new FlywayException("Unable to write to " + path + e.getMessage(), e, CoreErrorCode.CONFIGURATION);
        }
    }
}
