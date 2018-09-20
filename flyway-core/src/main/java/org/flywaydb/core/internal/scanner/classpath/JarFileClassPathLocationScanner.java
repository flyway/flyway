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

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.IOUtils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClassPathLocationScanner for jar files.
 */
public class JarFileClassPathLocationScanner implements ClassPathLocationScanner {
    private static final Log LOG = LogFactory.getLog(JarFileClassPathLocationScanner.class);

    /**
     * The separator that delimits the jar file name and the file inside the jar within a URL.
     */
    private final String separator;

    /**
     * @param separator The separator that delimits the jar file name and the file inside the jar within a URL.
     */
    JarFileClassPathLocationScanner(String separator) { this.separator = separator; }

    public Set<String> findResourceNames(String location, URL locationUrl) {
        JarFile jarFile;
        try {
            jarFile = getJarFromUrl(locationUrl);
        } catch (IOException e) {
            LOG.warn("Unable to determine jar from url (" + locationUrl + "): " + e.getMessage());
            return Collections.emptySet();
        }

        try {
            // For Tomcat and non-expanded WARs.
            String prefix = jarFile.getName().toLowerCase().endsWith(".war") ? "WEB-INF/classes/" : "";
            return findResourceNamesFromJarFile(jarFile, prefix, location);
        } finally {
            try {
                jarFile.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Retrieves the Jar file represented by this URL.
     *
     * @param locationUrl The URL of the jar.
     * @return The jar file.
     * @throws IOException when the jar could not be resolved.
     */
    private JarFile getJarFromUrl(URL locationUrl) throws IOException {
        URLConnection con = locationUrl.openConnection();
        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(false);
            return jarCon.getJarFile();
        }

        // No JarURLConnection -> need to resort to URL file parsing.
        // We'll assume URLs of the format "jar:path!/entry", with the protocol
        // being arbitrary as long as following the entry format.
        // We'll also handle paths with and without leading "file:" prefix.
        String urlFile = locationUrl.getFile();

        int separatorIndex = urlFile.indexOf(separator);
        if (separatorIndex != -1) {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            if (jarFileUrl.startsWith("file:")) {
                try {
                    return new JarFile(new URL(jarFileUrl).toURI().getSchemeSpecificPart());
                } catch (URISyntaxException ex) {
                    // Fallback for URLs that are not valid URIs (should hardly ever happen).
                    return new JarFile(jarFileUrl.substring("file:".length()));
                }
            }
            return new JarFile(jarFileUrl);
        }

        return new JarFile(urlFile);
    }

    /**
     * Finds all the resource names contained in this directory within this jar file.
     *
     * @param jarFile  The jar file.
     * @param prefix   The prefix to ignore within the jar file.
     * @param location The location to look under.
     * @return The resource names.
     */
    private Set<String> findResourceNamesFromJarFile(JarFile jarFile, String prefix, String location) {
        String toScan = prefix + location + (location.endsWith("/") ? "" : "/");
        Set<String> resourceNames = new TreeSet<>();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName();
            if (entryName.startsWith(toScan)) {
                resourceNames.add(entryName.substring(prefix.length()));
            }
        }

        return resourceNames;
    }
}