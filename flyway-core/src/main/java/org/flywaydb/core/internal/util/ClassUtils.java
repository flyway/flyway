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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
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
    @SuppressWarnings({"unchecked"})
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
     * @param className   The name of the class to load.
     * @param classLoader The ClassLoader to use.
     * @return the newly loaded class or {@code null} if it could not be loaded.
     */
    public static Class<?> loadClass(String className, ClassLoader classLoader) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnonymousClass()) {
                LOG.debug("Skipping non-instantiable class: " + className);
                return null;
            }

            clazz.getDeclaredConstructor().newInstance();
            LOG.debug("Found class: " + className);
            return clazz;
        } catch (InternalError e) {
            LOG.debug("Skipping invalid class: " + className);
            return null;
        } catch (IncompatibleClassChangeError e) {
            LOG.warn("Skipping incompatibly changed class: " + className);
            return null;
        } catch (NoClassDefFoundError e) {
            LOG.debug("Skipping non-loadable class definition: " + className);
            return null;
        } catch (ClassNotFoundException e) {
            LOG.debug("Skipping non-loadable class: " + className);
            return null;
        } catch (IllegalAccessException e) {
            LOG.debug("Skipping non-instantiable class (illegal access): " + className);
            return null;
        } catch (InstantiationException e) {
            LOG.debug("Skipping non-instantiable class (instantiation error): " + className);
            return null;
        } catch (NoSuchMethodException e) {
            LOG.debug("Skipping non-instantiable class (no default constructor): " + className);
            return null;
        } catch (InvocationTargetException e) {
            LOG.debug("Skipping non-instantiable class (invocation error): " + className);
            return null;
        }
    }

    /**
     * Computes the short name (name without package) of this class.
     *
     * @param aClass The class to analyse.
     * @return The short name.
     */
    public static String getShortName(Class<?> aClass) {
        String name = aClass.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * Retrieves the physical location on disk of this class.
     *
     * @param aClass The class to get the location for.
     * @return The absolute path of the .class file.
     */
    public static String getLocationOnDisk(Class<?> aClass) {
        try {
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
            String url = codeSource.getLocation().getPath();
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //Can never happen.
            return null;
        }
    }

    /**
     * Adds a jar or a directory with this name to the classpath.
     *
     * @param classLoader The current ClassLoader.
     * @param name        The name of the jar or directory to add.
     * @return The new ClassLoader containing the additional jar or directory.
     * @throws IOException when the jar or directory could not be found.
     */
    public static ClassLoader addJarOrDirectoryToClasspath(ClassLoader classLoader, String name) throws IOException {
        LOG.debug("Adding location to classpath: " + name);

        try {
            URL url = new File(name).toURI().toURL();
            return new URLClassLoader(new URL[]{url}, classLoader);
        } catch (Exception e) {
            throw new FlywayException("Unable to load " + name, e);
        }
    }
}