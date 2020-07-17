/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.scanner;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.internal.scanner.android.AndroidScanner;
import org.flywaydb.core.internal.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;
import org.flywaydb.core.internal.scanner.filesystem.FileSystemScanner;
import org.flywaydb.core.internal.scanner.s3.AwsS3Scanner;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

/**
 * Scanner for Resources and Classes.
 */
public class Scanner<I> implements ResourceProvider, ClassProvider<I> {
    private static final Log LOG = LogFactory.getLog(Scanner.class);

    private final List<LoadableResource> resources = new ArrayList<>();
    private final List<Class<? extends I>> classes = new ArrayList<>();

    // Lookup maps to speed up getResource
    private final HashMap<String, LoadableResource> relativeResourceMap = new HashMap<>();
    private HashMap<String, LoadableResource> absoluteResourceMap = null;

    /*
     * Constructor. Scans the given locations for resources, and classes implementing the specified interface.
     */
    public Scanner(Class<I> implementedInterface, Collection<Location> locations, ClassLoader classLoader, Charset encoding



            , ResourceNameCache resourceNameCache
            , LocationScannerCache locationScannerCache
    ) {
        FileSystemScanner fileSystemScanner = new FileSystemScanner(encoding



        );

        FeatureDetector detector =  new FeatureDetector(classLoader);
        boolean android = detector.isAndroidAvailable();
        boolean aws = detector.isAwsAvailable();

        for (Location location : locations) {
            if (location.isFileSystem()) {
                resources.addAll(fileSystemScanner.scanForResources(location));
            } else if (location.isAwsS3()) {
                if (aws) {
                    AwsS3Scanner awsS3Scanner = new AwsS3Scanner(encoding);
                    resources.addAll(awsS3Scanner.scanForResources(location));
                } else {
                    LOG.error("Can't read location " + location + "; AWS SDK not found");
                }
            } else {
                ResourceAndClassScanner<I> resourceAndClassScanner = android
                        ? new AndroidScanner<>(implementedInterface, classLoader, encoding, location)
                        : new ClassPathScanner<>(implementedInterface, classLoader, encoding, location, resourceNameCache, locationScannerCache);
                resources.addAll(resourceAndClassScanner.scanForResources());
                classes.addAll(resourceAndClassScanner.scanForClasses());
            }
        }

        for (LoadableResource resource : resources) {
            relativeResourceMap.put(resource.getRelativePath().toLowerCase(), resource);
        }
    }

    @Override
    public LoadableResource getResource(String name) {
        LoadableResource loadedResource = relativeResourceMap.get(name.toLowerCase());

        if (loadedResource != null) {
            return loadedResource;
        }

        // Only build the HashMap and resolve the absolute paths if an
        // absolute path is requested as this is really slow
        // Should only ever be required for sqlplus @
        if (Paths.get(name).isAbsolute()) {
            if (absoluteResourceMap == null) {
                absoluteResourceMap = new HashMap<>();
                for (LoadableResource resource : resources) {
                    absoluteResourceMap.put(resource.getAbsolutePathOnDisk().toLowerCase(), resource);
                }
            }

            loadedResource = absoluteResourceMap.get(name.toLowerCase());

            if (loadedResource != null) {
                return loadedResource;
            }
        }

        return null;
    }

    /**
     * Returns all known resources starting with the specified prefix and ending with any of the specified suffixes.
     *
     * @param prefix   The prefix of the resource names to match.
     * @param suffixes The suffixes of the resource names to match.
     * @return The resources that were found.
     */
    public Collection<LoadableResource> getResources(String prefix, String... suffixes) {
        List<LoadableResource> result = new ArrayList<>();
        for (LoadableResource resource : resources) {
            String fileName = resource.getFilename();
            if (StringUtils.startsAndEndsWith(fileName, prefix, suffixes)) {
                result.add(resource);
            } else {
                LOG.debug("Filtering out resource: " + resource.getAbsolutePath() + " (filename: " + fileName + ")");
            }
        }
        return result;
    }

    /**
     * Scans the classpath for concrete classes under the specified package implementing the specified interface.
     * Non-instantiable abstract classes are filtered out.
     *
     * @return The non-abstract classes that were found.
     */
    public Collection<Class<? extends I>> getClasses() {
        return Collections.unmodifiableCollection(classes);
    }
}