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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.exception.FlywayException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * A resource on the classpath.
 */
public class ClassPathResource implements Comparable<ClassPathResource> {
    /**
     * The location of the resource on the classpath.
     */
    private String location;

    /**
     * Creates a new ClassPathResource.
     *
     * @param location The location of the resource on the classpath.
     */
    public ClassPathResource(String location) {
        this.location = location;
    }

    /**
     * @return The location of the resource on the classpath.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Retrieves the location of this resource on disk.
     *
     * @return The location of this resource on disk.
     */
    public String getLocationOnDisk() {
        URL url = getUrl();
        if (url == null) {
            throw new FlywayException("Unable to location resource on disk: " + location);
        }
        try {
            return URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new FlywayException("Unknown encoding: UTF-8", e);
        }
    }

    /**
     * @return The url of this resource.
     */
    private URL getUrl() {
        return getClassLoader().getResource(location);
    }

    /**
     * @return The classloader to load the resource with.
     */
    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Loads this resource as a string.
     *
     * @param encoding The encoding to use.
     * @return The string contents of the resource.
     */
    public String loadAsString(String encoding) {
        try {
            InputStream inputStream = getClassLoader().getResourceAsStream(location);
            if (inputStream == null) {
                throw new FlywayException("Unable to obtain inputstream for resource: " + location);
            }
            Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));

            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new FlywayException("Unable to load resource: " + location + " (encoding: " + encoding + ")", e);
        }
    }

    /**
     * Loads this resource as a byte array.
     *
     * @return The contents of the resource.
     */
    public byte[] loadAsBytes() {
        try {
            InputStream inputStream = getClassLoader().getResourceAsStream(location);
            if (inputStream == null) {
                throw new FlywayException("Unable to obtain inputstream for resource: " + location);
            }
            return FileCopyUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            throw new FlywayException("Unable to load resource: " + location, e);
        }
    }

    /**
     * @return The filename of this resource.
     */
    public String getFilename() {
        return location.substring(location.lastIndexOf("/") + 1);
    }

    /**
     * Checks whether this resource exists.
     *
     * @return {@code true} if it exists, {@code false} if not.
     */
    public boolean exists() {
        return getUrl() != null;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassPathResource that = (ClassPathResource) o;

        if (!location.equals(that.location)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    public int compareTo(ClassPathResource o) {
        return location.compareTo(o.location);
    }
}
