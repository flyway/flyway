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
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    // Equinox "host" resource url pattern starts with bundleId, which is long according osgi core specification
    private static final Pattern EQUINOX_BUNDLE_ID_PATTERN = Pattern.compile("^\\d+");

    // #2198: Felix 6.0+ uses a "host" like e3a74e5a-af1f-46f0-bb53-bc5fee1b4a57_145.0 instead, where 145 is the bundle id.
    private static final Pattern FELIX_BUNDLE_ID_PATTERN = Pattern.compile(".*_(\\d+)\\..*");

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

    private Bundle getTargetBundleOrCurrent(Bundle currentBundle, URL locationUrl) {
        try {
            Bundle targetBundle = currentBundle.getBundleContext().getBundle(getBundleId(locationUrl.getHost()));
            return targetBundle != null ? targetBundle : currentBundle;
        } catch (Exception e) {
            return currentBundle;
        }
    }

    static long getBundleId(String host) {
        Matcher matcher = EQUINOX_BUNDLE_ID_PATTERN.matcher(host);
        if (matcher.find()) {
            return Double.valueOf(matcher.group()).longValue();
        } else {
            matcher = FELIX_BUNDLE_ID_PATTERN.matcher(host);
            if (matcher.find()) {
                return Double.valueOf(matcher.group(1)).longValue();
            }
        }
        throw new IllegalArgumentException("There's no bundleId in passed URL");
    }

    private String getPathWithoutLeadingSlash(URL entry) {
        final String path = entry.getPath();

        return path.startsWith("/") ? path.substring(1) : path;
    }
}