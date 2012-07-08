/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util.scanner;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * LocationScanner for jar files.
 */
public class JarFileLocationScanner implements LocationScanner {
    /**
     * The protocol used in the resource URL. Could be jar or zip when used from WebLogic.
     */
    private final String protocol;

    /**
     * Creates a new JarFileLocationScanner.
     * @param protocol The protocol used in the resource URL. Could be jar or zip when used from WebLogic.
     */
    public JarFileLocationScanner(String protocol) {
        this.protocol = protocol;
    }

    public Set<String> findResourceNames(String location, String locationUrl) throws IOException {
        return findResourceNamesFromJarFile(extractJarFileName(locationUrl), location);
    }

    /**
     * Extracts the Jar File name from this locationUrl.
     *
     * @param locationUrl The url returned from the classloader.
     * @return The jar file name.
     */
    /* private -> testing */ String extractJarFileName(String locationUrl) {
        int startPos = 0;
        if (locationUrl.startsWith(protocol)) {
            startPos = (protocol + ":").length();
        } else if (locationUrl.startsWith("file:")) {
            startPos = "file:".length();
        }
        return locationUrl.substring(startPos, locationUrl.lastIndexOf("!"));
    }

    /**
     * Finds all the resource names contained in this directory within this jar file.
     *
     * @param jarFileName The name of the jar file.
     * @param directory   The directory to look under.
     * @return The resource names.
     * @throws java.io.IOException when reading the jar file failed.
     */
    private Set<String> findResourceNamesFromJarFile(String jarFileName, String directory) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

        JarFile jarFile = new JarFile(jarFileName);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName();
            if (entryName.startsWith(directory)) {
                resourceNames.add(entryName);
            }
        }

        return resourceNames;
    }
}
