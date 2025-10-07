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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.scanner.FileLocation;
import org.flywaydb.core.internal.scanner.ReadWriteLocationHandler;

public class FilesystemLocationHandler implements ReadWriteLocationHandler {
    public static final String FILESYSTEM_PREFIX = "filesystem:";

    @Override
    public boolean canHandlePrefix(final String prefix) {
        return FILESYSTEM_PREFIX.equals(prefix);
    }

    @Override
    public Collection<LoadableResource> scanForResources(final Location location, final Configuration configuration) {
        final boolean stream = configuration.isStream();
        final FileSystemScanner fileSystemScanner = new FileSystemScanner(stream, configuration);
        return fileSystemScanner.scanForResources(location);
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
    public OutputStream getOutputStream(final FileLocation fileLocation, final Configuration configuration) {
        final File file;
        try {
            file = new File(ConfigUtils.getFilenameWithWorkingDirectory(fileLocation.path(),
                configuration)).getCanonicalFile();
        } catch (final IOException e) {
            throw new FlywayException("Unable to get canonical path for file "
                + fileLocation.path()
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
