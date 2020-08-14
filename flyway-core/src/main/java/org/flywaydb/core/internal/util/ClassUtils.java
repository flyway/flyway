/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for dealing with classes.
 */
public class ClassUtils {
    private static final Log LOG = LogFactory.getLog(ClassUtils.class);

    /**
     * Prevents instantiation.
     */
    private ClassUtils() {
        // Do nothing
    }

    /**
     * Creates a new instance of this class.
     *
     * @param className   The fully qualified name of the class to instantiate.
     * @param classLoader The ClassLoader to use.
     * @param <T>         The type of the new instance.
     * @return The new instance.
     * @throws FlywayException Thrown when the instantiation failed.
     */
    @SuppressWarnings({"unchecked"})
    // Must be synchronized for the Maven Parallel Junit runner to work
    public static synchronized <T> T instantiate(String className, ClassLoader classLoader) {
        try {
            return (T) Class.forName(className, true, classLoader).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + className + " : " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new instance of this class.
     *
     * @param clazz The class to instantiate.
     * @param <T>   The type of the new instance.
     * @return The new instance.
     * @throws FlywayException Thrown when the instantiation failed.
     */
    // Must be synchronized for the Maven Parallel Junit runner to work
    public static synchronized <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + clazz.getName() + " : " + e.getMessage(), e);
        }
    }

    /**
     * Instantiate all these classes.
     *
     * @param classes     A fully qualified class names to instantiate.
     * @param classLoader The ClassLoader to use.
     * @param <T>         The common type for all classes.
     * @return The list of instances.
     */
    public static <T> List<T> instantiateAll(String[] classes, ClassLoader classLoader) {
        List<T> clazzes = new ArrayList<>();
        for (String clazz : classes) {
            if (StringUtils.hasLength(clazz)) {
                clazzes.add(ClassUtils.<T>instantiate(clazz, classLoader));
            }
        }
        return clazzes;
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param className   The name of the class to check.
     * @param classLoader The ClassLoader to use.
     * @return whether the specified class is present
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            classLoader.loadClass(className);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Loads the class with this name using the class loader.
     *
     * @param implementedInterface The interface the class is expected to implement.
     * @param className            The name of the class to load.
     * @param classLoader          The ClassLoader to use.
     * @return the newly loaded class or {@code null} if it could not be loaded.
     */
    public static <I> Class<? extends I> loadClass(Class<I> implementedInterface, String className, ClassLoader classLoader) {
        try {
            Class<?> clazz = classLoader.loadClass(className);

            if (!implementedInterface.isAssignableFrom(clazz)) {
                return null;
            }

            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnonymousClass()) {
                LOG.debug("Skipping non-instantiable class: " + className);
                return null;
            }

            clazz.getDeclaredConstructor().newInstance();
            LOG.debug("Found class: " + className);
            //noinspection unchecked
            return (Class<? extends I>) clazz;
        } catch (Throwable e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            LOG.warn("Skipping " + className + ": " + formatThrowable(e) + (
                    rootCause == e
                            ? ""
                            : " caused by " + formatThrowable(rootCause)
                            + " at " + ExceptionUtils.getThrowLocation(rootCause)
            ));
            return null;
        }
    }

    private static String formatThrowable(Throwable e) {
        return "(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
    }

    /**
     * Retrieves the physical location on disk of this class.
     *
     * @param aClass The class to get the location for.
     * @return The absolute path of the .class file.
     */
    public static String getLocationOnDisk(Class<?> aClass) {
        ProtectionDomain protectionDomain = aClass.getProtectionDomain();
        if (protectionDomain == null) {
            //Android
            return null;
        }
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            //Custom classloader with for example classes defined using URLClassLoader#defineClass(String name, byte[] b, int off, int len)
            return null;
        }
        return UrlUtils.decodeURL(codeSource.getLocation().getPath());
    }

    /**
     * Adds these jars or directories to the classpath.
     *
     * @param classLoader The current ClassLoader.
     * @param jarFiles    The jars or directories to add.
     * @return The new ClassLoader containing the additional jar or directory.
     */
    public static ClassLoader addJarsOrDirectoriesToClasspath(ClassLoader classLoader, List<File> jarFiles) {
        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
            LOG.debug("Adding location to classpath: " + jarFile.getAbsolutePath());

            try {
                urls.add(jarFile.toURI().toURL());
            } catch (Exception e) {
                throw new FlywayException("Unable to load " + jarFile.getPath(), e);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]), classLoader);
    }


    /**
     * Gets the String value of a static field.
     *
     * @param className   The fully qualified name of the class to instantiate.
     * @param classLoader The ClassLoader to use.
     * @param fieldName   The field name
     * @return The value of the field.
     * @throws FlywayException Thrown when the instantiation failed.
     */
    public static String getStaticFieldValue(String className, String fieldName, ClassLoader classLoader) {
        try {
            Class clazz = Class.forName(className, true, classLoader);
            Field field = clazz.getField(fieldName);
            return (String)field.get(null);
        } catch (Exception e) {
            throw new FlywayException("Unable to obtain field value " + className + "." + fieldName + " : " + e.getMessage(), e);
        }
    }
}