/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.scanner.classpath;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.UrlUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.internal.util.scanner.classpath.jboss.JBossVFSv3ClassPathLocationScanner;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
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

    /**
     * Cache location lookups.
     */
    private final Map<Location, List<URL>> locationUrlCache = new HashMap<Location, List<URL>>();

    /**
     * Cache location scanners.
     */
    private final Map<String, ClassPathLocationScanner> locationScannerCache = new HashMap<String, ClassPathLocationScanner>();

    /**
     * Cache resource names.
     */
    private final Map<ClassPathLocationScanner, Map<URL, Set<String>>> resourceNameCache = new HashMap<ClassPathLocationScanner, Map<URL, Set<String>>>();

    /**
     * Creates a new Classpath scanner.
     *
     * @param classLoader The ClassLoader for loading migrations on the classpath.
     */
    public ClassPathScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Resource[] scanForResources(Location path, String prefix, String suffix) throws IOException {
        LOG.debug("Scanning for classpath resources at '" + path + "' (Prefix: '" + prefix + "', Suffix: '" + suffix + "')");

        Set<Resource> resources = new TreeSet<Resource>();

        Set<String> resourceNames = findResourceNames(path, prefix, suffix);
        for (String resourceName : resourceNames) {
            resources.add(new ClassPathResource(resourceName, classLoader));
            LOG.debug("Found resource: " + resourceName);
        }

        return resources.toArray(new Resource[resources.size()]);
    }

    @Override
    public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception {
        LOG.debug("Scanning for classes at '" + location + "' (Implementing: '" + implementedInterface.getName() + "')");

        List<Class<?>> classes = new ArrayList<Class<?>>();

        Set<String> resourceNames = findResourceNames(location, "", ".class");
        for (String resourceName : resourceNames) {
            String className = toClassName(resourceName);
            Class<?> clazz;

            try {
                clazz = classLoader.loadClass(className);

                if (!implementedInterface.isAssignableFrom(clazz)) {
                    continue;
                }

                if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnonymousClass()) {
                    LOG.debug("Skipping non-instantiable class: " + className);
                    continue;
                }

                ClassUtils.instantiate(className, classLoader);
            } catch (InternalError e) {
                LOG.debug("Skipping invalid class: " + className);
                continue;
            } catch (IncompatibleClassChangeError e) {
                LOG.debug("Skipping incompatibly changed class: " + className);
                continue;
            } catch (NoClassDefFoundError e) {
                LOG.debug("Skipping non-loadable class: " + className);
                continue;
            } catch (Exception e) {
                throw new FlywayException("Unable to instantiate class: " + className, e);
            }

            classes.add(clazz);
            LOG.debug("Found class: " + className);
        }

        return classes.toArray(new Class<?>[classes.size()]);
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
     * @param location The location on the classpath to scan.
     * @param prefix   The filename prefix to match.
     * @param suffix   The filename suffix to match.
     * @return The resource names.
     * @throws IOException when scanning this location failed.
     */
    private Set<String> findResourceNames(Location location, String prefix, String suffix) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

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

        // Make an additional attempt at finding resources in jar files that don't contain directory entries
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            for (URL url : urlClassLoader.getURLs()) {
                if ("file".equals(url.getProtocol())
                        && url.getPath().endsWith(".jar")
                        && !url.getPath().matches(".*" + Pattern.quote("/jre/lib/") + ".*")) {
                    // All non-system jars on disk
                    JarFile jarFile;
                    try {
                        jarFile = new JarFile(url.toURI().getSchemeSpecificPart());
                    } catch (URISyntaxException ex) {
                        // Fallback for URLs that are not valid URIs (should hardly ever happen).
                        jarFile = new JarFile(url.getPath().substring("file:".length()));
                    }

                    try {
                        boolean directoryFound = false;
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            if (entries.nextElement().isDirectory()) {
                                directoryFound = true;
                                break;
                            }
                        }
                        if (!directoryFound) {
                            entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                String entryName = entries.nextElement().getName();
                                if (entryName.startsWith(location.getPath())) {
                                    locationResolved = true;
                                    if (entryName.endsWith(suffix)) {
                                        resourceNames.add(entryName);
                                    }
                                }
                            }
                        }
                    } finally {
                        jarFile.close();
                    }
                }
            }
        }

        if (!locationResolved) {
            LOG.warn("Unable to resolve location " + location);
        }

        return filterResourceNames(resourceNames, prefix, suffix);
    }

    /**
     * Gets the physical location urls for this logical path on the classpath.
     *
     * @param location The location on the classpath.
     * @return The underlying physical URLs.
     * @throws IOException when the lookup fails.
     */
    private List<URL> getLocationUrlsForPath(Location location) throws IOException {
        if (locationUrlCache.containsKey(location)) {
            return locationUrlCache.get(location);
        }

        LOG.debug("Determining location urls for " + location + " using ClassLoader " + classLoader + " ...");

        List<URL> locationUrls = new ArrayList<URL>();

        if (classLoader.getClass().getName().startsWith("com.ibm")) {
            // WebSphere
            Enumeration<URL> urls = classLoader.getResources(location.getPath() + "/flyway.location");
            if (!urls.hasMoreElements()) {
                LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + ")"
                        + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!");
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                locationUrls.add(new URL(URLDecoder.decode(url.toExternalForm(), "UTF-8").replace("/flyway.location", "")));
            }
        } else {
            Enumeration<URL> urls = classLoader.getResources(location.getPath());
            while (urls.hasMoreElements()) {
                locationUrls.add(urls.nextElement());
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
            resourceNameCache.put(locationScanner, new HashMap<URL, Set<String>>());
            return locationScanner;
        }

        if ("jar".equals(protocol)
                || "war".equals(protocol)
                || "zip".equals(protocol) //WebLogic
                || "wsjar".equals(protocol) //WebSphere
                ) {
            JarFileClassPathLocationScanner locationScanner = new JarFileClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<URL, Set<String>>());
            return locationScanner;
        }

        FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if (featureDetector.isJBossVFSv3Available() && "vfs".equals(protocol)) {
            JBossVFSv3ClassPathLocationScanner locationScanner = new JBossVFSv3ClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<URL, Set<String>>());
            return locationScanner;
        }
        if (featureDetector.isOsgiFrameworkAvailable() && (
                "bundle".equals(protocol) // Felix
                        || "bundleresource".equals(protocol)) //Equinox
                ) {
            OsgiClassPathLocationScanner locationScanner = new OsgiClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<URL, Set<String>>());
            return locationScanner;
        }

        return null;
    }

    /**
     * Filters this list of resource names to only include the ones whose filename matches this prefix and this suffix.
     *
     * @param resourceNames The names to filter.
     * @param prefix        The prefix to match.
     * @param suffix        The suffix to match.
     * @return The filtered names set.
     */
    private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String suffix) {
        Set<String> filteredResourceNames = new TreeSet<String>();
        for (String resourceName : resourceNames) {
            String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            if (fileName.startsWith(prefix) && fileName.endsWith(suffix)
                    && (fileName.length() > (prefix + suffix).length())) {
                filteredResourceNames.add(resourceName);
            } else {
                LOG.debug("Filtering out resource: " + resourceName + " (filename: " + fileName + ")");
            }
        }
        return filteredResourceNames;
    }
}
