/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * OSGi specific scanner that performs the migration search in
 * the current bundle's classpath.
 *
 * <p>
 * The resources that this scanner returns can only be loaded if
 * Flyway's ClassLoader has access to the bundle that contains the migrations.
 * </p>
 */
public class OsgiClassPathLocationScanner implements ClassPathLocationScanner {
    public Set<String> findResourceNames(String location, URL locationUrl) {
        Set<String> resourceNames = new TreeSet<>();

        Bundle bundle = getTargetBundleOrCurrent(FrameworkUtil.getBundle(getClass()), locationUrl);
        @SuppressWarnings({"unchecked"})
        Enumeration<URL> entries = bundle.findEntries(locationUrl.getPath(), "*", true);

        if (entries != null) {
            while (entries.hasMoreElements()) {
                URL entry = entries.nextElement();
                String resourceName = getPathWithoutLeadingSlash(entry);

                resourceNames.add(resourceName);
            }
        }

        return resourceNames;
    }

    // Search through all bundles until we find one with the right host
    private Bundle getTargetBundleOrCurrent(Bundle currentBundle, URL locationUrl) {
        final String path = locationUrl.getPath();
        final String host = locationUrl.getHost();
        return Arrays.stream(currentBundle.getBundleContext().getBundles())
                .map(b -> new AbstractMap.SimpleEntry<>(b, b.getEntry(path)))
                .filter(pair -> Objects.nonNull(pair.getValue()))
                .filter(pair -> pair.getValue().getHost().equals(host))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(currentBundle);
    }

    private String getPathWithoutLeadingSlash(URL entry) {
        final String path = entry.getPath();

        return path.startsWith("/") ? path.substring(1) : path;
    }
}