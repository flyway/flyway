/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-scanners
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
package org.flywaydb.scanners;

import static org.flywaydb.scanners.ScannerUtils.validateMigrationNaming;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.experimental.migration.ExperimentalMigrationScanner;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;
import org.flywaydb.core.internal.scanner.filesystem.DirectoryValidationResult;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

@CustomLog
public abstract class BaseSqlMigrationScanner implements ExperimentalMigrationScanner {
   
    protected Collection<Pair<LoadableResource, SqlScriptMetadata>> scanFromFileSystem(final File dir, final Location location, final Configuration configuration, final ParsingContext parsingContext) {
        final DirectoryValidationResult validationResult = getDirectoryValidationResult(dir);
        final String fileOrClasspath = location.isFileSystem() ? "filesystem" : "classpath";
        if (validationResult != DirectoryValidationResult.VALID) {
            if (configuration.isFailOnMissingLocations()) {
                throw new FlywayException("Failed to find " + fileOrClasspath + " location: "
                                              + location.getRootPath()
                                              + " ("
                                              + validationResult
                                              + ")");
            }

            String message = "Skipping " + fileOrClasspath + " location: " + location.getRootPath() + " (" + validationResult + ")";
            if (location.isFileSystem()) {
                LOG.error(message);
            } else {
                LOG.debug(message);
            }

            return Collections.emptyList();
        }

        final Set<String> resourceNames = findResourceNamesFromFileSystem(location.getRootPath(),
            dir,
            configuration.isFailOnMissingLocations(),
            configuration.isValidateMigrationNaming(),
            new ResourceNameParser(configuration),
            location.isFileSystem(),
            configuration.getSqlMigrationSuffixes());
        return resourceNames.stream()
            .filter(path -> matchesPath(path, location))
            .map(resourceName -> processResource(
                location,
                configuration,
                resourceName,
                parsingContext)).toList();
    }
    abstract boolean matchesPath(String path, Location location);

    private Pair<LoadableResource, SqlScriptMetadata> processResource(final Location location,
        final Configuration configuration,
        final String resourceName,
        final ParsingContext parsingContext) {
        boolean detectEncodingForThisResource = configuration.isDetectEncoding();
        Charset encoding = configuration.getEncoding();
        String encodingBlurb = "";
        SqlScriptMetadata metadata = null;
        if (new File(resourceName + ".conf").exists()) {
            metadata = getSqlScriptMetadata(location, configuration, resourceName, parsingContext);
            if (metadata.encoding() != null) {
                encoding = Charset.forName(metadata.encoding());
                detectEncodingForThisResource = false;
                encodingBlurb = " (with overriding encoding " + encoding + ")";
            }
        }
        final String fileOrClasspath = location.isFileSystem() ? "filesystem" : "classpath";
        LOG.debug("Found " + fileOrClasspath + " resource: " + resourceName + encodingBlurb);

        final FileSystemResource fileSystemResource = new FileSystemResource(location,
            resourceName,
            encoding,
            detectEncodingForThisResource,
            configuration.isStream());
        return Pair.of(fileSystemResource, metadata);
    }

    private SqlScriptMetadata getSqlScriptMetadata(final Location location,
        final Configuration configuration,
        final String resourceName,
        final ParsingContext parsingContext) {
        final LoadableResource metadataResource = new FileSystemResource(location,
            resourceName + ".conf",
            configuration.getEncoding(),
            false);
        return SqlScriptMetadata.fromResource(metadataResource,
            new MetadataParser(configuration, parsingContext),
            configuration);
    }
    
    private static class MetadataParser extends Parser {
        private MetadataParser(final Configuration configuration,
            final ParsingContext parsingContext) {
            super(configuration, parsingContext, 0);
        }
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
        final boolean validateMigrationNaming,
        final ResourceNameParser resourceNameParser,
        final boolean isFileSystem,
        final String... sqlMigrationSuffixes) {
        final String path = folder.getPath();
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        final Set<String> resourceNames = new TreeSet<>();
        final String fileOrClasspath = isFileSystem ? "filesystem" : "classpath";
        final File[] files = folder.listFiles();

        if (files == null) {
            if (throwOnMissingLocations) {
                throw new FlywayException("Failed to find " + fileOrClasspath + " location: "
                                              + path
                                              + " ("
                                              + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER
                                              + ")");
            }

            String message = "Skipping " + fileOrClasspath + " location: " + path + " ("
                + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER + ")";

            if (isFileSystem) {
                LOG.error(message);
            } else {
                LOG.debug(message);
            }

            return Collections.emptySet();
        }

        final List<Pair<File, ResourceName>> fileList = Arrays.stream(files)
            .filter(File::canRead)
            .map(file -> Pair.of(file, resourceNameParser.parse(file.getName()))).toList();

        final List<Pair<String, ResourceName>> resources = fileList.stream()
            .map(pair -> Pair.of(pair.getLeft().getName(), pair.getRight())).toList();
        validateMigrationNaming(resources, validateMigrationNaming, sqlMigrationSuffixes);

        fileList.stream().filter(pair -> pair.getRight().isValid() && !"".equals(pair.getRight().getSuffix()))
              .forEach(pair -> resourceNames.add(pair.getLeft().getPath()));

        Arrays.stream(files).filter(File::canRead).filter(File::isDirectory).forEach(file -> {
            if (file.isHidden()) {
                // #1807: Skip hidden directories to avoid issues with Kubernetes
                LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
            } else {
                resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation,
                    file,
                    throwOnMissingLocations,
                    validateMigrationNaming,
                    resourceNameParser, isFileSystem,
                    sqlMigrationSuffixes));
            }
        });

        return resourceNames;
    }
}
