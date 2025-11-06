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
package org.flywaydb.core.internal.scanner.classpath;

import static org.flywaydb.core.api.CoreLocationPrefix.CLASSPATH_PREFIX;

import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.ClasspathLocationHandler;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;

public class ClasspathLocationHandlerImpl implements ClasspathLocationHandler {
    @Getter
    private final String prefix = CLASSPATH_PREFIX;

    private final ResourceNameCache resourceNameCache = new ResourceNameCache();
    private final LocationScannerCache locationScannerCache = new LocationScannerCache();

    @Override
    public Collection<LoadableResource> scanForResources(final Location location, final Configuration configuration) {
        final ClassPathScanner<JavaMigration> classPathScanner = createClassPathScanner(location, configuration);
        return classPathScanner.scanForResources();
    }

    @Override
    public Optional<LoadableResource> getResource(final Location location, final Configuration configuration) {
        final String packagePath = location.getRootPath().substring(0, location.getRootPath().lastIndexOf("/"));
        final ClassPathScanner<JavaMigration> classPathScanner = createClassPathScanner(Location.fromPath(
            CLASSPATH_PREFIX,
            packagePath), configuration);
        return classPathScanner.getResource(location);
    }

    @Override
    public <I> Collection<Class<? extends I>> scanForClasses(final Class<I> clazz,
        final Location location,
        final Configuration configuration) {
        final ResourceAndClassScanner<I> classPathScanner = new ClassPathScanner<>(clazz,
            configuration.getClassLoader(),
            configuration.getEncoding(),
            location,
            resourceNameCache,
            locationScannerCache,
            configuration.isFailOnMissingLocations(),
            configuration.isStream());
        return classPathScanner.scanForClasses();
    }

    @Override
    public boolean handlesWildcards() {
        return true;
    }

    @Override
    public String getPathSeparator() {
        return "/";
    }

    @Override
    public String normalizePath(final String path) {
        final var pathWithoutPrefix = path.startsWith("/") ? path.substring(1) : path;
        return pathWithoutPrefix.endsWith("/")
            ? pathWithoutPrefix.substring(0, pathWithoutPrefix.length() - 1)
            : pathWithoutPrefix;
    }

    private ClassPathScanner<JavaMigration> createClassPathScanner(final Location location,
        final Configuration configuration) {
        return new ClassPathScanner<>(JavaMigration.class,
            configuration.getClassLoader(),
            configuration.getEncoding(),
            location,
            resourceNameCache,
            locationScannerCache,
            configuration.isFailOnMissingLocations(),
            configuration.isStream());
    }
}
