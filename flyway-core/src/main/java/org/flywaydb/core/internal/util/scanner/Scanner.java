/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.util.scanner;

import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.android.AndroidScanner;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemScanner;

/**
 * Scanner for Resources and Classes.
 */
public class Scanner {
    private final ClassLoader classLoader;

    public Scanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Scans this location for resources, starting with the specified prefix and ending with the specified suffix.
     *
     * @param location The location to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffix   The suffix of the resource names to match.
     * @return The resources that were found.
     * @throws java.io.IOException when the location could not be scanned.
     */
    public Resource[] scanForResources(Location location, String prefix, String suffix) throws Exception {
        if (location.isFileSystem()) {
            return new FileSystemScanner().scanForResources(location.getPath(), prefix, suffix);
        }

        if (new FeatureDetector(classLoader).isAndroidAvailable()) {
            return new AndroidScanner(classLoader).scanForResources(location.getPath(), prefix, suffix);
        }

        return new ClassPathScanner(classLoader).scanForResources(location.getPath(), prefix, suffix);
    }


    /**
     * Scans the classpath for concrete classes under the specified package implementing this interface.
     * Non-instantiable abstract classes are filtered out.
     *
     * @param location             The location (package) in the classpath to start scanning.
     *                             Subpackages are also scanned.
     * @param implementedInterface The interface the matching classes should implement.
     * @return The non-abstract classes that were found.
     * @throws Exception when the location could not be scanned.
     */
    public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception {
        if (new FeatureDetector(classLoader).isAndroidAvailable()) {
            return new AndroidScanner(classLoader).scanForClasses(location.getPath(), implementedInterface);
        }

        return new ClassPathScanner(classLoader).scanForClasses(location.getPath(), implementedInterface);
    }
}