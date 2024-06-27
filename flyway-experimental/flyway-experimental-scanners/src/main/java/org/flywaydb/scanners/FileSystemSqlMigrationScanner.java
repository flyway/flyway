/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-scanners
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
package org.flywaydb.scanners;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.experimental.ExperimentalMigrationScanner;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;
import org.flywaydb.core.internal.scanner.filesystem.DirectoryValidationResult;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.Pair;

@CustomLog
public class FileSystemSqlMigrationScanner implements ExperimentalMigrationScanner {

    @Override
    public Collection<LoadableResource> scan(final Location location, final Configuration configuration) {
        final String path = location.getRootPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "'");

        final File dir = new File(path);

        final DirectoryValidationResult validationResult = getDirectoryValidationResult(dir);

        if (validationResult != DirectoryValidationResult.VALID) {
            if (configuration.isFailOnMissingLocations()) {
                throw new FlywayException("Failed to find filesystem location: " + path + " (" + validationResult + ")");
            }

            LOG.error("Skipping filesystem location: " + path + " (" + validationResult + ")");
            return Collections.emptyList();
        }

        final Set<String> resourceNames = findResourceNamesFromFileSystem(path,
                                                                          dir,
                                                                          configuration.isFailOnMissingLocations(),
                                                                          new ResourceNameParser(configuration));
        return resourceNames.stream()
                            .filter(location::matchesPath)
                            .map(resourceName -> processResource(location,
                                                                 configuration,
                                                                 resourceName))
                            .toList();
    }

    private static LoadableResource processResource(final Location location,
                                  final Configuration configuration,
                                  final String resourceName) {
        boolean detectEncodingForThisResource = configuration.isDetectEncoding();
        Charset encoding = configuration.getEncoding();
        String encodingBlurb = "";
        
        if (new File(resourceName + ".conf").exists()) {
            final SqlScriptMetadata metadata = getSqlScriptMetadata(location, configuration, resourceName);
            if (metadata.encoding() != null) {
                encoding = Charset.forName(metadata.encoding());
                detectEncodingForThisResource = false;
                encodingBlurb = " (with overriding encoding " + encoding + ")";
            }
        }
        LOG.debug("Found filesystem resource: " + resourceName + encodingBlurb);
        
        return new FileSystemResource(location, resourceName,
                                             encoding,
                                             detectEncodingForThisResource,
                                             configuration.isStream());

    }

    private static SqlScriptMetadata getSqlScriptMetadata(final Location location,
                                                          final Configuration configuration,
                                                          final String resourceName) {
        final LoadableResource metadataResource = new FileSystemResource(location,
                                                                         resourceName + ".conf",
                                                                         configuration.getEncoding(),
                                                                         false);
        return SqlScriptMetadata.fromResource(metadataResource,null, configuration);
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

    private Set<String> findResourceNamesFromFileSystem(final String scanRootLocation,
                                                        final File folder,
                                                        final boolean throwOnMissingLocations,
                                                        final ResourceNameParser resourceNameParser) {
        final String path = folder.getPath();
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        final Set<String> resourceNames = new TreeSet<>();

        final File[] files = folder.listFiles();

        if (files == null) {
            if (throwOnMissingLocations) {
                throw new FlywayException("Failed to find filesystem location: " + path + " (" + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER + ")");
            }

            LOG.error("Skipping filesystem location: " + path + " (" + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER + ")");
            return Collections.emptySet();
        }

        Arrays.stream(files)
              .filter(File::canRead)
              .map(file -> Pair.of(file, resourceNameParser.parse(file.getName())))
              .filter(pair -> pair.getRight().isValid() && !"".equals(pair.getRight().getSuffix()))
              .forEach(pair -> resourceNames.add(pair.getLeft().getPath()));

        Arrays.stream(files)
              .filter(File::canRead)
              .filter(File::isDirectory)
              .forEach(file -> {
                  if (file.isHidden()) {
                      // #1807: Skip hidden directories to avoid issues with Kubernetes
                      LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
                  } else {
                      resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation, file, throwOnMissingLocations, resourceNameParser));
                  }
              });

        return resourceNames;
    }
}
