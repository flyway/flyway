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
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.classpath.ClassPathScanner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ClasspathClassScanner {

    private final ResourceNameCache resourceNameCache = new ResourceNameCache();
    private final LocationScannerCache locationScannerCache = new LocationScannerCache();

    private final ClassLoader classLoader;

    public ClasspathClassScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<String> scanForType(String location, Class<?> classType, boolean errorOnNotFound) {
        ClassPathScanner<?> s = new ClassPathScanner<>(classType, classLoader, Charset.defaultCharset(), new Location("classpath:" + location),
                resourceNameCache, locationScannerCache, errorOnNotFound);

        List<String> discoveredTypes = new ArrayList<>();
        for (LoadableResource resource : s.scanForResources()) {
            if (resource.getAbsolutePath().endsWith(".class")) {

                discoveredTypes.add(toClassName(resource.getAbsolutePath()));
            }
        }

        return discoveredTypes;
    }

    private String toClassName(String resourceName) {
        String nameWithDots = resourceName.replace("/", ".");
        return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }

}