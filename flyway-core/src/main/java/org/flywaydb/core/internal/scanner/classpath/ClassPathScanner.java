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

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv3ClassPathLocationScanner;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.UrlUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * ClassPath scanner.
 */
public class ClassPathScanner implements ResourceAndClassScanner {
    private static final Log LOG = LogFactory.getLog(ClassPathScanner.class);

    /**
     * The ClassLoader for loading migrations on the classpath.
     */
    private final ClassLoader classLoader;
    private final Location location;

    private final Set<LoadableResource> resources = new TreeSet<>();

    /**
     * Cache location lookups.
     */
    private final Map<Location, List<URL>> locationUrlCache = new HashMap<>();

    /**
     * Cache location scanners.
     */
    private final Map<String, ClassPathLocationScanner> locationScannerCache = new HashMap<>();

    /**
     * Cache resource names.
     */
    private final Map<ClassPathLocationScanner, Map<URL, Set<String>>> resourceNameCache = new HashMap<>();

    /**
     * Creates a new Classpath scanner.
     *
     * @param classLoader The ClassLoader for loading migrations on the classpath.
     */
    public ClassPathScanner(ClassLoader classLoader, Charset encoding, Location location) {
        this.classLoader = classLoader;
        this.location = location;

        LOG.debug("Scanning for classpath resources at '" + location + "' ...");
        for (String resourceName : findResourceNames()) {
            resources.add(new ClassPathResource(location, resourceName, classLoader, encoding));
            LOG.debug("Found resource: " + resourceName);
        }
    }

    @Override
    public Collection<LoadableResource> scanForResources() {
        return resources;
    }

    @Override
    public Collection<Class<?>> scanForClasses() {
        LOG.debug("Scanning for classes at " + location);

        List<Class<?>> classes = new ArrayList<Class<?>>();

        for (LoadableResource resource : resources) {
            if (resource.getAbsolutePath().endsWith(".class")) {
                Class<?> clazz = ClassUtils.loadClass(toClassName(resource.getAbsolutePath()), classLoader);
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
    private String toClassName(String resourceName) {
        String nameWithDots = resourceName.replace("/", ".");
        return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }

    /**
     * Finds the resources names present at this location and below on the classpath starting with this prefix and
     * ending with this suffix.
     *
     * @return The resource names.
     */
    private Set<String> findResourceNames() {
        Set<String> resourceNames = new TreeSet<>();

        List<URL> locationUrls = getLocationUrlsForPath(location);
        for (URL locationUrl : locationUrls) {
            LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

            UrlResolver urlResolver = createUrlResolver(locationUrl.getProtocol());
            URL resolvedUrl = urlResolver.toStandardJavaUrl(locationUrl);

            String protocol = resolvedUrl.getProtocol();
            ClassPathLocationScanner classPathLocationScanner = createLocationScanner(protocol);
            if (classPathLocationScanner == null) {
                String scanRoot = UrlUtils.toFilePath(resolvedUrl);
                LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + protocol + ")");
            } else {
                Set<String> names = resourceNameCache.get(classPathLocationScanner).get(resolvedUrl);
                if (names == null) {
                    names = classPathLocationScanner.findResourceNames(location.getPath(), resolvedUrl);
                    resourceNameCache.get(classPathLocationScanner).put(resolvedUrl, names);
                }
                resourceNames.addAll(names);
            }
        }

        boolean locationResolved = !locationUrls.isEmpty();

        if (!locationResolved) {
            // Make an additional attempt at finding resources in jar files
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                for (URL url : urlClassLoader.getURLs()) {
                    if ("file".equals(url.getProtocol())
                            && url.getPath().endsWith(".jar")
                            && !url.getPath().matches(".*" + Pattern.quote("/jre/lib/") + ".*")) {
                        // All non-system jars on disk
                        JarFile jarFile;
                        try {
                            try {
                                jarFile = new JarFile(url.toURI().getSchemeSpecificPart());
                            } catch (URISyntaxException ex) {
                                // Fallback for URLs that are not valid URIs (should hardly ever happen).
                                jarFile = new JarFile(url.getPath().substring("file:".length()));
                            }
                        } catch (IOException e) {
                            LOG.warn("Skipping unloadable jar file: " + url + " (" + e.getMessage() + ")");
                            continue;
                        } catch (SecurityException e) {
                            LOG.warn("Skipping unloadable jar file: " + url + " (" + e.getMessage() + ")");
                            continue;
                        }

                        try {
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                String entryName = entries.nextElement().getName();
                                if (entryName.startsWith(location.getPath())) {
                                    locationResolved = true;
                                    resourceNames.add(entryName);
                                }
                            }
                        } finally {
                            try {
                                jarFile.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        }

        if (!locationResolved) {
            LOG.warn("Unable to resolve location " + location);
        }

        return resourceNames;
    }

    /**
     * Gets the physical location urls for this logical path on the classpath.
     *
     * @param location The location on the classpath.
     * @return The underlying physical URLs.
     */
    private List<URL> getLocationUrlsForPath(Location location) {
        if (locationUrlCache.containsKey(location)) {
            return locationUrlCache.get(location);
        }

        LOG.debug("Determining location urls for " + location + " using ClassLoader " + classLoader + " ...");

        List<URL> locationUrls = new ArrayList<>();

        if (classLoader.getClass().getName().startsWith("com.ibm")) {
            // WebSphere
            Enumeration<URL> urls;
            try {
                urls = classLoader.getResources(location.getPath() + "/flyway.location");
                if (!urls.hasMoreElements()) {
                    LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + ")"
                            + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!");
                }
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    locationUrls.add(new URL(URLDecoder.decode(url.toExternalForm(), "UTF-8").replace("/flyway.location", "")));
                }
            } catch (IOException e) {
                LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + ")"
                        + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!");
            }
        } else {
            Enumeration<URL> urls;
            try {
                urls = classLoader.getResources(location.getPath());
                while (urls.hasMoreElements()) {
                    locationUrls.add(urls.nextElement());
                }
            } catch (IOException e) {
                LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + "): " + e.getMessage());
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
    private UrlResolver createUrlResolver(String protocol) {
        if (new FeatureDetector(classLoader).isJBossVFSv2Available() && protocol.startsWith("vfs")) {
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
    private ClassPathLocationScanner createLocationScanner(String protocol) {
        if (locationScannerCache.containsKey(protocol)) {
            return locationScannerCache.get(protocol);
        }

        if ("file".equals(protocol)) {
            FileSystemClassPathLocationScanner locationScanner = new FileSystemClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        if ("jar".equals(protocol) || isTomcat(protocol) || isWebLogic(protocol) || isWebSphere(protocol)) {
            String separator = isTomcat(protocol) ? "*/" : "!/";
            ClassPathLocationScanner locationScanner = new JarFileClassPathLocationScanner(separator);
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if (featureDetector.isJBossVFSv3Available() && "vfs".equals(protocol)) {
            JBossVFSv3ClassPathLocationScanner locationScanner = new JBossVFSv3ClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }
        if (featureDetector.isOsgiFrameworkAvailable() && (isFelix(protocol) || isEquinox(protocol))) {
            OsgiClassPathLocationScanner locationScanner = new OsgiClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        return null;
    }

    private boolean isEquinox(String protocol) {
        return "bundleresource".equals(protocol);
    }

    private boolean isFelix(String protocol) {
        return "bundle".equals(protocol);
    }

    private boolean isWebSphere(String protocol) {
        return "wsjar".equals(protocol);
    }

    private boolean isWebLogic(String protocol) {
        return "zip".equals(protocol);
    }

    private boolean isTomcat(String protocol) {
        return "war".equals(protocol);
    }
}