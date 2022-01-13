/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import org.flywaydb.core.api.FlywayException;

import java.beans.Expression;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassUtils {
    /**
     * Creates a new instance of this class.
     *
     * @param className The fully qualified name of the class to instantiate.
     * @param classLoader The ClassLoader to use.
     * @return The new instance.
     * @throws FlywayException Thrown when the instantiation failed.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T instantiate(String className, ClassLoader classLoader) {
        try {
            return (T) Class.forName(className, true, classLoader).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + className + " : " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T instantiate(String className, ClassLoader classLoader, Object... params) {
        try {
            return (T) new Expression(Class.forName(className, false, classLoader), "new", params).getValue();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + className + " : " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new instance of this class.
     *
     * @param clazz The class to instantiate.
     * @return The new instance.
     * @throws FlywayException Thrown when the instantiation failed.
     */
    public static <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + clazz.getName() + " : " + e.getMessage(), e);
        }
    }

    /**
     * Instantiate all these classes.
     *
     * @param classes Fully qualified class names to instantiate.
     * @param classLoader The ClassLoader to use.
     * @return The list of instances.
     */
    public static <T> List<T> instantiateAll(String[] classes, ClassLoader classLoader) {
        List<T> clazzes = new ArrayList<>();
        for (String clazz : classes) {
            if (StringUtils.hasLength(clazz)) {
                clazzes.add(ClassUtils.instantiate(clazz, classLoader));
            }
        }
        return clazzes;
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param className The name of the class to check.
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
     * Determine whether a class implementing the service identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either no class is found, or the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param serviceName The name of the service to check.
     * @param classLoader The ClassLoader to use.
     * @return whether an implementation of the specified service is present
     */
    public static boolean isImplementationPresent(String serviceName, ClassLoader classLoader) {
        try {
            Class service = classLoader.loadClass(serviceName);
            return ServiceLoader.load(service).iterator().hasNext();
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Loads the class with this name using the class loader.
     *
     * @param implementedInterface The interface the class is expected to implement.
     * @param className The name of the class to load.
     * @param classLoader The ClassLoader to use.
     * @return the newly loaded class or {@code null} if it could not be loaded.
     * @throws Exception Skip if exception thrown
     */
    public static <I> Class<? extends I> loadClass(Class<I> implementedInterface, String className, ClassLoader classLoader) throws Exception {
        Class<?> clazz = classLoader.loadClass(className);
        if (!implementedInterface.isAssignableFrom(clazz)) {
            return null;
        }

        if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnonymousClass()) {
            return null;
        }

        clazz.getDeclaredConstructor().newInstance();
        //noinspection unchecked
        return (Class<? extends I>) clazz;
    }

    public static String formatThrowable(Throwable e) {
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
            return null;
        }
        CodeSource codeSource = protectionDomain.getCodeSource();

        if (codeSource == null || codeSource.getLocation() == null) {
            //Custom classloader with for example classes defined using URLClassLoader#defineClass(String name, byte[] b, int off, int len)
            return null;
        }
        return UrlUtils.decodeURL(codeSource.getLocation().getPath());
    }

    /**
     * Adds these jars or directories to the classpath.
     *
     * @param classLoader The current ClassLoader.
     * @param jarFiles The jars or directories to add.
     * @return The new ClassLoader containing the additional jar or directory.
     */
    public static ClassLoader addJarsOrDirectoriesToClasspath(ClassLoader classLoader, List<File> jarFiles) {
        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
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
     * @param className The fully qualified name of the class to instantiate.
     * @param fieldName The field name.
     * @param classLoader The ClassLoader to use.
     * @return The value of the field.
     * @throws FlywayException Thrown when the instantiation failed.
     */
    public static String getStaticFieldValue(String className, String fieldName, ClassLoader classLoader) {
        try {
            Class clazz = Class.forName(className, true, classLoader);
            Field field = clazz.getField(fieldName);
            return (String) field.get(null);
        } catch (Exception e) {
            throw new FlywayException("Unable to obtain field value " + className + "." + fieldName + " : " + e.getMessage(), e);
        }
    }
}