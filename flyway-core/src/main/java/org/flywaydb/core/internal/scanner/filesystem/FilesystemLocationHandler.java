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
import java.util.Collection;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.ReadOnlyLocationHandler;

public class FilesystemLocationHandler implements ReadOnlyLocationHandler {
    private static final String FILESYSTEM_PREFIX = "filesystem:";

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
}
