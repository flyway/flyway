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
package org.flywaydb.core.experimental.migration;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.Pair;

public class ExperimentalMigrationScannerManager {
    private final List<? extends ExperimentalMigrationScanner> scanners;

    public ExperimentalMigrationScannerManager(final List<? extends ExperimentalMigrationScanner> scanners) {
        this.scanners = scanners;
    }

    public Collection<LoadableResourceMetadata> scan(final Configuration configuration, final ParsingContext parsingContext) {
        final List<LoadableResourceMetadata> resources = Arrays.stream(configuration.getLocations())
                                                               .flatMap(location -> scan(location,configuration).stream())
                                                               .map(resource -> getLoadableResourceMetadata(resource, configuration, parsingContext))
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

    private static LoadableResourceMetadata getLoadableResourceMetadata(final Pair<LoadableResource, SqlScriptMetadata> resource,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        
        final ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);
        final ResourceName resourceName = resourceNameParser.parse(resource.getLeft().getFilename());
        final int checksum = getChecksumForLoadableResource(
            Objects.equals(resourceName.getPrefix(), configuration.getRepeatableSqlMigrationPrefix()),
            resource.getLeft(),
            resourceName,
            configuration,
            parsingContext);
        return new LoadableResourceMetadata(
            resourceName.getVersion(),
            resourceName.getDescription(),
            resourceName.getPrefix(),
            resource.getLeft(),
            resource.getRight(),
            checksum);
    }

    private static Integer getChecksumForLoadableResource(
        final boolean repeatable,
        final LoadableResource resource,
        final ResourceName resourceName,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        if (repeatable && configuration.isPlaceholderReplacement()) {
            parsingContext.updateFilenamePlaceholder(resourceName, configuration);
            return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResource(resource, configuration, parsingContext));
        }
        return ChecksumCalculator.calculate(resource);
    }
    private static LoadableResource createPlaceholderReplacingLoadableResource(
        final LoadableResource loadableResource,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        
        return new LoadableResource() {
            @Override
            public Reader read() {
                return PlaceholderReplacingReader.create(configuration, parsingContext, loadableResource.read());
            }

            @Override
            public String getAbsolutePath() {return loadableResource.getAbsolutePath();}

            @Override
            public String getAbsolutePathOnDisk() {return loadableResource.getAbsolutePathOnDisk();}

            @Override
            public String getFilename() {return loadableResource.getFilename();}

            @Override
            public String getRelativePath() {return loadableResource.getRelativePath();}
        };        
    }
    

    private Collection<Pair<LoadableResource, SqlScriptMetadata>> scan(final Location location, final Configuration configuration) {
        return scanners.stream()
                       .flatMap(scanner -> scanner.scan(location, configuration).stream())
                       .toList();
    }
}
