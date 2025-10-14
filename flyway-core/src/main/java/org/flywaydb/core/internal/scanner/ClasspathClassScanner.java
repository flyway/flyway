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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;

public class ClasspathClassScanner {
    private final ResourceNameCache resourceNameCache = new ResourceNameCache();
    private final LocationScannerCache locationScannerCache = new LocationScannerCache();

    private final ClassLoader classLoader;

    public ClasspathClassScanner(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<String> scanForType(final String location, final Class<?> classType, final boolean errorOnNotFound) {
        final ResourceAndClassScanner<?> s = new ClassPathScanner<>(classType,
            classLoader,
            Charset.defaultCharset(),
            LocationParser.parseLocation("classpath:" + location),
            resourceNameCache,
            locationScannerCache,
            errorOnNotFound,
            false);

        final List<String> discoveredTypes = new ArrayList<>();
        for (final LoadableResource resource : s.scanForResources()) {
            if (resource.getAbsolutePath().endsWith(".class")) {

                discoveredTypes.add(toClassName(resource.getAbsolutePath()));
            }
        }

        return discoveredTypes;
    }

    private String toClassName(final String resourceName) {
        final String nameWithDots = resourceName.replace("/", ".");
        return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }
}
