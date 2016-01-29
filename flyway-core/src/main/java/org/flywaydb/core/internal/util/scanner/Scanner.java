/**
 * Copyright 2010-2015 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.util.scanner.classpath.ResourceAndClassScanner;
import org.flywaydb.core.internal.util.scanner.classpath.android.AndroidScanner;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemScanner;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Scanner for Resources and Classes.
 */
public class Scanner {
    private final ResourceAndClassScanner resourceAndClassScanner;

    private final ClassLoader classLoader;
    private final FileSystemScanner fileSystemScanner = new FileSystemScanner();

    private Scanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
        if (new FeatureDetector(classLoader).isAndroidAvailable()) {
            resourceAndClassScanner = new AndroidScanner(classLoader);
        } else {
            resourceAndClassScanner = new ClassPathScanner(classLoader);
        }
    }

    private static Map<ClassLoader, WeakReference<Scanner>> scannerCache = new WeakHashMap<ClassLoader, WeakReference<Scanner>>();

    /**
     * Create or retrieves a new instance of Scanner. Instances are weakly cached with
     * their classpath as key.
     * @param classLoader The classloader to scan.
     * @return A new or existing Scanner instance, never null.
     */
    public static synchronized Scanner create(ClassLoader classLoader) {
        WeakReference<Scanner> result = scannerCache.get(classLoader);

        if (result == null || result.get() == null) {
            return createNew(classLoader);
        }

        return result.get();
    }

    /**
     * Creates a new instance of Scanner. Instances are weakly cached with
     * their classpath as key. This method is necessary when flyway is methods are called a) multiple times
     * in one run, b) using the same classloader and c) when the classpath of the classloader changes between
     * invocations. This can happen for example in the Maven plugin in a "clean migrate" scenario, where between
     * the clean step an the migrate step source code is compiled / copied and the classloader extended (with the
     * target/classes folder, Maven 2 did create a new classloader, thus the problem did not occur).
     * @param classLoader The classloader to scan.
     * @return A new Scanner instance, never null.
     */
    public static synchronized Scanner createNew(ClassLoader classLoader) {
        WeakReference<Scanner> result = new WeakReference<Scanner>(new Scanner(classLoader));
        scannerCache.put(classLoader, result);

        return result.get();
    }

    /**
     * Scans this location for resources, starting with the specified prefix and ending with the specified suffix.
     *
     * @param location The location to start searching. Subdirectories are also searched.
     * @param prefix   The prefix of the resource names to match.
     * @param suffix   The suffix of the resource names to match.
     * @return The resources that were found.
     */
    public Resource[] scanForResources(Location location, String prefix, String suffix) {
        try {
            if (location.isFileSystem()) {
                return fileSystemScanner.scanForResources(location, prefix, suffix);
            }
            return resourceAndClassScanner.scanForResources(location, prefix, suffix);
        } catch (Exception e) {
            throw new FlywayException("Unable to scan for SQL migrations in location: " + location, e);
        }
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
        return resourceAndClassScanner.scanForClasses(location, implementedInterface);
    }

    /**
     * @return The class loader used for scanning.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}