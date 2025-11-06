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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import lombok.CustomLog;
import org.flywaydb.core.api.CoreLocationPrefix;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv3ClassPathLocationScanner;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.UrlUtils;

@CustomLog
public class ClassPathScanner<I> implements ResourceAndClassScanner<I> {
    private final Class<? extends I> implementedInterface;
    private final ClassLoader classLoader;
    private final Location location;
    private final Collection<ClassPathResource> resources = new HashSet<>();
    /**
     * Cache location lookups.
     */
    private final Map<Location, List<URL>> locationUrlCache = new HashMap<>();
    /**
     * Cache location scanners.
     */
    private final LocationScannerCache locationScannerCache;
    /**
     * Cache resource names.
     */
    private final ResourceNameCache resourceNameCache;
    /**
     * Whether to throw an exception if a location was not found.
     */
    private final boolean throwOnMissingLocations;

    public ClassPathScanner(final Class<? extends I> implementedInterface,
        final ClassLoader classLoader,
        final Charset encoding,
        final Location location,
        final ResourceNameCache resourceNameCache,
        final LocationScannerCache locationScannerCache,
        final boolean throwOnMissingLocations,
        final boolean stream) {
        this.implementedInterface = implementedInterface;
        this.classLoader = classLoader;
        this.location = location;
        this.resourceNameCache = resourceNameCache;
        this.locationScannerCache = locationScannerCache;
        this.throwOnMissingLocations = throwOnMissingLocations;

        LOG.debug("Scanning for classpath resources at '" + location + "' ...");
        for (final Pair<String, String> resourceNameAndParentURL : findResourceNamesAndParentURLs()) {
            final String resourceName = resourceNameAndParentURL.getLeft();
            final String parentURL = resourceNameAndParentURL.getRight();
            resources.add(new ClassPathResource(location, resourceName, classLoader, encoding, parentURL, stream));
            LOG.debug("Found resource: " + resourceNameAndParentURL.getLeft());
        }
    }

    @Override
    public Collection<LoadableResource> scanForResources() {
        return resources.stream().map(LoadableResource.class::cast).toList();
    }

    public Optional<LoadableResource> getResource(final Location location) {
        final var name = location.getRootPath().substring(location.getRootPath().lastIndexOf("/") + 1);
        return resources.stream()
            .filter(x -> x.getFilename().equals(name))
            .map(LoadableResource.class::cast)
            .findFirst();
    }

    @Override
    public Collection<Class<? extends I>> scanForClasses() {
        LOG.debug("Scanning for classes at " + location);

        final Collection<Class<? extends I>> classes = new ArrayList<>();

        for (final LoadableResource resource : resources) {
            if (resource.getAbsolutePath().endsWith(".class")) {
                Class<? extends I> clazz;
                try {
                    clazz = ClassUtils.loadClass(implementedInterface,
                        toClassName(resource.getAbsolutePath()),
                        classLoader);
                } catch (final Throwable e) {
                    final Throwable rootCause = ExceptionUtils.getRootCause(e);
                    LOG.warn("Skipping " + Callback.class + ": " + ClassUtils.formatThrowable(e) + (rootCause == e
                        ? ""
                        : " caused by "
                            + ClassUtils.formatThrowable(rootCause)
                            + " at "
                            + ExceptionUtils.getThrowLocation(rootCause)));
                    clazz = null;
                }
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }

    /**
     * Converts this resource name to a fully qualified class name.
     *
     * @param resourceName The resource name.
     * @return The class name.
     */
    private String toClassName(final String resourceName) {
        final String nameWithDots = resourceName.replace("/", ".");
        return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }

    private Set<Pair<String, String>> findResourceNamesAndParentURLs() {
        final Set<Pair<String, String>> resourceNamesAndParentURLs = new TreeSet<>();

        final List<URL> locationUrls = getLocationUrlsForPath(location);
        for (final URL locationUrl : locationUrls) {
            LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

            final UrlResolver urlResolver = createUrlResolver(locationUrl.getProtocol());
            final URL resolvedUrl = urlResolver.toStandardJavaUrl(locationUrl);

            final String protocol = resolvedUrl.getProtocol();
            final ClassPathLocationScanner classPathLocationScanner = createLocationScanner(protocol);
            if (classPathLocationScanner == null) {
                final String scanRoot = UrlUtils.toFilePath(resolvedUrl);
                LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + protocol + ")");
            } else {
                Set<String> names = resourceNameCache.get(classPathLocationScanner, resolvedUrl);
                if (names == null) {
                    names = classPathLocationScanner.findResourceNames(location.getRootPath(), resolvedUrl);
                    resourceNameCache.put(classPathLocationScanner, resolvedUrl, names);
                }
                final Collection<String> filteredNames = new HashSet<>();
                for (final String name : names) {
                    if (matchesAnyWildcardRestrictions(location, name)) {
                        filteredNames.add(name);
                    }
                }

                for (final String filteredName : filteredNames) {
                    resourceNamesAndParentURLs.add(Pair.of(filteredName, resolvedUrl.getPath()));
                }
            }
        }

        // Make an additional attempt at finding resources in jar files in case the URL scanning method above didn't
        // yield any results.
        boolean locationResolved = !locationUrls.isEmpty();

        // Starting with Java 11, resources at the root of the classpath aren't being found using the URL scanning
        // method above and we need to revert to Jar file walking.
        final boolean isClassPathRoot = CoreLocationPrefix.CLASSPATH_PREFIX.equals(location.getPrefix()) && "".equals(
            location.getRootPath());

        if (!locationResolved || isClassPathRoot) {
            if (classLoader instanceof final URLClassLoader urlClassLoader) {
                for (final URL url : urlClassLoader.getURLs()) {
                    if ("file".equals(url.getProtocol()) && url.getPath().endsWith(".jar") && !url.getPath()
                        .matches(".*" + Pattern.quote("/jre/lib/") + ".*")) {
                        // All non-system jars on disk
                        JarFile jarFile;
                        try {
                            try {
                                jarFile = new JarFile(url.toURI().getSchemeSpecificPart());
                            } catch (final URISyntaxException ex) {
                                // Fallback for URLs that are not valid URIs (should hardly ever happen).
                                jarFile = new JarFile(url.getPath().substring("file:".length()));
                            }
                        } catch (final IOException | SecurityException e) {
                            LOG.warn("Skipping unloadable jar file: " + url + " (" + e.getMessage() + ")");
                            continue;
                        }

                        try {
                            final Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                final String entryName = entries.nextElement().getName();
                                if (entryName.startsWith(location.getRootPath())) {
                                    locationResolved = true;
                                    resourceNamesAndParentURLs.add(Pair.of(entryName, url.getPath()));
                                }
                            }
                        } finally {
                            try {
                                jarFile.close();
                            } catch (final IOException e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        }

        if (!locationResolved) {
            final String message = "Unable to resolve location " + location + ".";

            if (throwOnMissingLocations) {
                throw new FlywayException(message);
            } else {
                LOG.debug(message);
            }
        }

        return resourceNamesAndParentURLs;
    }

    private static Boolean matchesAnyWildcardRestrictions(final Location location, final String path) {
        return Optional.ofNullable(location.getPathRegex()).map(x -> x.matcher(path).matches()).orElse(true);
    }

    /**
     * Gets the physical location urls for this logical path on the classpath.
     *
     * @param location The location on the classpath.
     * @return The underlying physical URLs.
     */
    private List<URL> getLocationUrlsForPath(final Location location) {
        if (locationUrlCache.containsKey(location)) {
            return locationUrlCache.get(location);
        }

        LOG.debug("Determining location urls for " + location + " using ClassLoader " + classLoader + " ...");

        final List<URL> locationUrls = new ArrayList<>();

        if (classLoader.getClass().getName().startsWith("com.ibm")) {
            // WebSphere
            final Enumeration<URL> urls;
            try {
                urls = classLoader.getResources(location.getRootPath() + "/flyway.location");
                if (!urls.hasMoreElements()) {
                    LOG.error("Unable to resolve location "
                        + location
                        + " (ClassLoader: "
                        + classLoader
                        + ")"
                        + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!");
                }
                while (urls.hasMoreElements()) {
                    final URL url = urls.nextElement();
                    locationUrls.add(new URL(UrlUtils.decodeURL(url.toExternalForm()).replace("/flyway.location", "")));
                }
            } catch (final IOException e) {
                LOG.error("Unable to resolve location "
                    + location
                    + " (ClassLoader: "
                    + classLoader
                    + ")"
                    + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!");
            }
        } else {
            final Enumeration<URL> urls;
            try {
                urls = classLoader.getResources(location.getRootPath());
                while (urls.hasMoreElements()) {
                    locationUrls.add(urls.nextElement());
                }
            } catch (final IOException e) {
                LOG.error("Unable to resolve location "
                    + location
                    + " (ClassLoader: "
                    + classLoader
                    + "): "
                    + e.getMessage()
                    + ".");
            }
        }

        locationUrlCache.put(location, locationUrls);

        return locationUrls;
    }

    /**
     * Creates an appropriate URL resolver scanner for this url protocol.
     *
     * @param protocol The protocol of the location url to scan.
     * @return The url resolver for this protocol.
     */
    private UrlResolver createUrlResolver(final String protocol) {
        if (protocol.startsWith("vfs") && new FeatureDetector(classLoader).isJBossVFSv2Available()) {
            return new JBossVFSv2UrlResolver();
        }

        return new DefaultUrlResolver();
    }

    /**
     * Creates an appropriate location scanner for this url protocol.
     *
     * @param protocol The protocol of the location url to scan.
     * @return The location scanner or {@code null} if it could not be created.
     */
    private ClassPathLocationScanner createLocationScanner(final String protocol) {
        if (locationScannerCache.containsKey(protocol)) {
            return locationScannerCache.get(protocol);
        }

        if ("file".equals(protocol)) {
            final ClassPathLocationScanner locationScanner = new FileSystemClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        if ("jar".equals(protocol) || isTomcat(protocol) || isWebLogic(protocol) || isWebSphere(protocol)) {
            final String separator = isTomcat(protocol) ? "*/" : "!/";
            final ClassPathLocationScanner locationScanner = new JarFileClassPathLocationScanner(separator);
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        final FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if ("vfs".equals(protocol) && featureDetector.isJBossVFSv3Available()) {
            final ClassPathLocationScanner locationScanner = new JBossVFSv3ClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }
        if ((isFelix(protocol) || isEquinox(protocol)) && featureDetector.isOsgiFrameworkAvailable()) {
            final ClassPathLocationScanner locationScanner = new OsgiClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        return null;
    }

    private boolean isEquinox(final String protocol) {
        return "bundleresource".equals(protocol);
    }

    private boolean isFelix(final String protocol) {
        return "bundle".equals(protocol);
    }

    private boolean isWebSphere(final String protocol) {
        return "wsjar".equals(protocol);
    }

    private boolean isWebLogic(final String protocol) {
        return "zip".equals(protocol);
    }

    private boolean isTomcat(final String protocol) {
        return "war".equals(protocol);
    }
}
