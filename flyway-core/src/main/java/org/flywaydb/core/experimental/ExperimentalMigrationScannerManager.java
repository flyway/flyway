/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.experimental;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;

public class ExperimentalMigrationScannerManager {
    private final List<? extends ExperimentalMigrationScanner> scanners;

    public ExperimentalMigrationScannerManager(final List<? extends ExperimentalMigrationScanner> scanners) {
        this.scanners = scanners;
    }

    public Collection<LoadableResourceMetadata> scan(final Configuration configuration) {
        final List<LoadableResourceMetadata> resources = Arrays.stream(configuration.getLocations())
                                                               .flatMap(location -> scan(location,configuration).stream())
                                                               .map(resource -> getLoadableResourceMetadata(resource, configuration))
                                                               .toList();

        final Collection<LoadableResourceMetadata> resourceSet = new HashSet<>();
        resources.forEach(resource -> {
            if(resourceSet.contains(resource)) {
                final LoadableResourceMetadata first = resourceSet.stream()
                                                                  .filter(loadableResourceMetadata ->
                                                                              loadableResourceMetadata.equals(resource))
                                                                  .findFirst()
                                                                  .get();
                if (first.version() != null) {
                    throw new FlywayException(String.format(
                        "Found more than one migration with version %s\nOffenders:\n-> %s \n-> %s",
                        resource.version(),
                        resource.loadableResource().getAbsolutePath(),
                        first.loadableResource().getAbsolutePath()), CoreErrorCode.DUPLICATE_VERSIONED_MIGRATION);
                } else {
                    throw new FlywayException(String.format("Found more than one repeatable migration with description '%s'\nOffenders:\n-> %s \n-> %s ",
                                                            resource.description(),
                                                            resource.loadableResource().getAbsolutePath(),
                                                            first.loadableResource().getAbsolutePath()), CoreErrorCode.DUPLICATE_REPEATABLE_MIGRATION);
                }
            } else {
                resourceSet.add(resource);
            }
        });
        
        return resources;              
    }

    private static LoadableResourceMetadata getLoadableResourceMetadata(final LoadableResource resource,
                                                                        final Configuration configuration) {
        final ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);
        final ResourceName resourceName = resourceNameParser.parse(resource.getFilename());
        return new LoadableResourceMetadata(resourceName.getVersion(), resourceName.getDescription(), resourceName.getPrefix(), resource);
    }

    private Collection<LoadableResource> scan(final Location location, final Configuration configuration) {
        return scanners.stream()
                       .flatMap(scanner -> scanner.scan(location, configuration).stream())
                       .toList();
    }
}
