/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.core.internal.scanner.classpath;

import lombok.CustomLog;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ClassPathLocationScanner for images compiled by GraalVM's native-image tool.
 */
@CustomLog
public class NativeImageClassPathLocationScanner implements ClassPathLocationScanner {

    private FileSystemProvider fileSystemProvider;

    private Set<String> doFindResourceNames(String location, URI locationUri, FileSystem fileSystem) {
        Path fileSystemRoot;
        try {
            fileSystemRoot = fileSystem.getPath("/");
        } catch (IllegalArgumentException | FileSystemNotFoundException e) {
            // We've checked that we have native-image specific FS support for resources before creating this
            //  scanner, but just in case...
            LOG.warn("Failed to construct Path object for location (" + locationUri + "): " + e.getMessage());
            return Collections.emptySet();
        }

        Path scanRoot;

        try {
            scanRoot = fileSystemRoot.resolve(location);
        } catch (InvalidPathException e) {
            LOG.warn(
                    "Location " + location + " is not resolvable against native-image file system root: "
                            + e.getMessage()
            );
            return Collections.emptySet();
        }

        if (!Files.isDirectory(scanRoot)) {
            LOG.debug("Skipping path as it is not a directory: " + scanRoot);
            return Collections.emptySet();
        }

        LOG.debug("Scanning for native-image resources at: " + scanRoot);

        try {
            try (Stream<Path> stream = Files.walk(scanRoot)) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(fileSystemRoot::relativize)
                        .map(Path::toString)
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            LOG.warn("Failed to access file at location (" + locationUri + "): " + e.getMessage());
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> findResourceNames(String location, URL locationUrl) {
        URI uri = URI.create("resource:/");

        if (fileSystemProvider == null) {
            String scheme = uri.getScheme();

            fileSystemProvider = FileSystemProvider.installedProviders().stream()
                    .filter(provider -> scheme.equalsIgnoreCase(provider.getScheme()))
                    .findFirst()
                    .orElse(null);

            if (fileSystemProvider == null) {
                // We've checked that we have native-image specific FS support for resources before creating this
                //  scanner, but just in case...
                LOG.warn("Failed to find file system provider for native-image resource file system");
                return Collections.emptySet();
            }
        }

        try (FileSystem fileSystem = fileSystemProvider.newFileSystem(uri, Collections.emptyMap())) {
            return doFindResourceNames(location, uri, fileSystem);
        } catch (IllegalArgumentException | IOException e) {
            LOG.warn("Failed to create file system at location (" + locationUrl + "): " + e.getMessage());
            return Collections.emptySet();
        } catch (FileSystemAlreadyExistsException e) {
            // NB: This code assumes that resource file system is not open. Existing file system may be closed by
            //  another thread while we're scanning (if it does not belong to us). So, ask API user to close it before
            //  interacting with Flyway API.
            LOG.warn("Native-image resource file system is open. Please close it before using Flyway");
            return Collections.emptySet();
        }
    }
}
