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
package org.flywaydb.core.internal.scanner;

import java.util.Collection;
import java.util.Optional;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.extensibility.Plugin;

public interface ReadOnlyLocationHandler extends Plugin {
    default boolean canHandleLocation(final Location location) {
        return getPrefix().equals(location.getPrefix());
    }

    String getPrefix();

    Collection<LoadableResource> scanForResources(Location location, final Configuration configuration);

    Optional<LoadableResource> getResource(Location location, final Configuration configuration);

    boolean handlesWildcards();

    String getPathSeparator();

    String normalizePath(String path);
}
