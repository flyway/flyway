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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import lombok.CustomLog;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * Scanner for Resources and Classes.
 */
@CustomLog
public class Scanner<I> implements ResourceProvider, ClassProvider<I> {

    private final Collection<LoadableResource> resources = new ArrayList<>();
    private final Collection<Class<? extends I>> classes = new ArrayList<>();

    // Lookup maps to speed up getResource
    private final HashMap<String, LoadableResource> relativeResourceMap = new HashMap<>();
    private HashMap<String, LoadableResource> absoluteResourceMap;

    public Scanner(final Class<? extends I> implementedInterface,
        final Configuration configuration,
        final Location[] locations) {
        final Collection<ReadOnlyLocationHandler> locationHandlers = configuration.getPluginRegister()
            .getInstancesOf(ReadOnlyLocationHandler.class);
        Arrays.stream(locations)
            .forEach(location -> locationHandlers.stream()
                .filter(x -> x.canHandleLocation(location))
                .findFirst()
                .ifPresent(readOnlyLocationHandler -> resources.addAll(readOnlyLocationHandler.scanForResources(location,
                    configuration))));

        for (final LoadableResource resource : resources) {
            relativeResourceMap.put(resource.getRelativePath().toLowerCase(Locale.ROOT), resource);
        }

        final Collection<ClasspathLocationHandler> classpathLocationHandlers = configuration.getPluginRegister()
            .getInstancesOf(ClasspathLocationHandler.class);
        Arrays.stream(locations)
            .forEach(location -> classpathLocationHandlers.stream()
                .filter(x -> x.canHandleLocation(location))
                .findFirst()
                .ifPresent(classpathLocationHandler -> classes.addAll(classpathLocationHandler.scanForClasses(
                    implementedInterface,
                    location,
                    configuration))));
    }

    @Override
    public LoadableResource getResource(final String name) {
        LoadableResource loadedResource = relativeResourceMap.get(name.toLowerCase(Locale.ROOT));

        if (loadedResource != null) {
            return loadedResource;
        }

        // Only build the HashMap and resolve the absolute paths if an
        // absolute path is requested as this is really slow
        // Should only ever be required for sqlplus @
        if (Paths.get(name).isAbsolute()) {
            if (absoluteResourceMap == null) {
                absoluteResourceMap = new HashMap<>();
                for (final LoadableResource resource : resources) {
                    absoluteResourceMap.put(resource.getAbsolutePathOnDisk().toLowerCase(Locale.ROOT), resource);
                }
            }

            loadedResource = absoluteResourceMap.get(name.toLowerCase(Locale.ROOT));

            return loadedResource;
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
    public Collection<LoadableResource> getResources(final String prefix, final String... suffixes) {
        final Collection<LoadableResource> result = new ArrayList<>();
        for (final LoadableResource resource : resources) {
            final String fileName = resource.getFilename();
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
