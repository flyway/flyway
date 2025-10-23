/*-
 * ========================LICENSE_START=================================
 * flyway-nc-scanners
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
package org.flywaydb.scanners;

import static org.flywaydb.scanners.ScannerUtils.validateMigrationNaming;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import lombok.CustomLog;
import org.flywaydb.core.api.CoreLocationPrefix;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.Pair;

@CustomLog
public class ClasspathSqlMigrationScanner extends BaseSqlMigrationScanner {
    @Override
    public Collection<Pair<LoadableResource, SqlScriptMetadata>> scan(final Location location,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        if (!CoreLocationPrefix.CLASSPATH_PREFIX.equals(location.getPrefix())) {
            return List.of();
        }

        LOG.debug("Scanning for classpath resources at '" + location.getRootPath() + "'");

        final ClassLoader classLoader = configuration.getClassLoader();
        final Collection<URL> locationUrls = new ArrayList<>();
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

        if (locationUrls.isEmpty()) {
            if (configuration.isFailOnMissingLocations()) {
                throw new FlywayException("Failed to find classpath location: " + location.getRootPath());
            }

            LOG.debug("Skipping classpath location: " + location.getRootPath());
            return Collections.emptyList();
        }

        final Collection<Pair<LoadableResource, SqlScriptMetadata>> classPathMigrations = new ArrayList<>();
        for (final URL locationUrl : locationUrls) {
            LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

            final String protocol = locationUrl.getProtocol();
            if ("file".equals(protocol)) {
                classPathMigrations.addAll(scanFromFileSystem(new File(locationUrl.getPath()),
                    location,
                    configuration,
                    parsingContext));
            } else if ("jar".equals(protocol)) {
                classPathMigrations.addAll(scanFromJarFile(location, locationUrl, configuration, parsingContext));
            }
        }

        return classPathMigrations;
    }

    @Override
    boolean matchesPath(final String path, final Location location) {
        final String rootPath = new File(Thread.currentThread()
            .getContextClassLoader()
            .getResource(".")
            .getPath()).getAbsolutePath();
        final String remainingPath = path.replace("\\", "/").substring(rootPath.length() + 1);

        return matchesAnyWildcardRestrictions(location, remainingPath);
    }

    private static Boolean matchesAnyWildcardRestrictions(final Location location, final String path) {
        return Optional.ofNullable(location.getPathRegex()).map(x -> x.matcher(path).matches()).orElse(true);
    }

    private Collection<Pair<LoadableResource, SqlScriptMetadata>> scanFromJarFile(final Location location,
        final URL locationUrl,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        final Set<String> resourceNames = findResourceNames(location.getRootPath(), locationUrl);

        final List<Pair<LoadableResource, SqlScriptMetadata>> fileList = resourceNames.stream()
            .map(resourceName -> processJarResource(location, locationUrl, configuration, resourceName, parsingContext))
            .toList();

        final List<Pair<String, ResourceName>> resources = fileList.stream()
            .map(x -> Pair.of(x.getLeft().getFilename(),
                new ResourceNameParser(configuration).parse(x.getLeft().getFilename())))
            .toList();
        validateMigrationNaming(resources,
            configuration.isValidateMigrationNaming(),
            configuration.getSqlMigrationSuffixes());

        return fileList.stream().filter(x -> {
            final ResourceName name = new ResourceNameParser(configuration).parse(x.getLeft().getFilename());
            return name.isValid() && !"".equals(name.getSuffix());
        }).collect(Collectors.toSet());
    }

    private Pair<LoadableResource, SqlScriptMetadata> processJarResource(final Location location,
        final URL locationUrl,
        final Configuration configuration,
        final String resourceName,
        final ParsingContext parsingContext) {

        final ClassPathResource classPathResource = new ClassPathResource(location,
            resourceName,
            configuration.getClassLoader(),
            configuration.getEncoding(),
            locationUrl.getPath(),
            configuration.isStream());
        return Pair.of(classPathResource, null);
    }

    private Set<String> findResourceNames(final String location, final URL locationUrl) {
        final JarFile jarFile;
        try {
            final URLConnection con = locationUrl.openConnection();
            if (con instanceof final JarURLConnection jarCon) {
                jarCon.setUseCaches(false);
                jarFile = jarCon.getJarFile();
            } else {
                return Collections.emptySet();
            }
        } catch (final IOException e) {
            LOG.warn("Unable to determine jar from url (" + locationUrl + "): " + e.getMessage());
            return Collections.emptySet();
        }

        try {
            return findResourceNamesFromJarFile(jarFile, location);
        } finally {
            try {
                jarFile.close();
            } catch (final IOException ignored) {
            }
        }
    }

    private Set<String> findResourceNamesFromJarFile(final JarFile jarFile, final String location) {
        final String toScan = location + (location.endsWith("/") ? "" : "/");
        final Set<String> resourceNames = new TreeSet<>();

        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            final String entryName = entry.getName();
            if (entryName.startsWith(toScan)) {
                resourceNames.add(entryName);
            }
        }

        return resourceNames;
    }
}
