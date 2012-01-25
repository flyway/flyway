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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
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
        return url.getPath();
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
            Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));
            return copyToString(reader);
        } catch (IOException e) {
            throw new FlywayException("Unable to load resource: " + location + " (encoding: " + encoding + ")", e);
        }
    }

    /**
     * Copy the contents of the given Reader into a String.
     * Closes the reader when done.
     *
     * @param in the reader to copy from
     * @return the String that has been copied to
     * @throws IOException in case of I/O errors
     */
    private static String copyToString(Reader in) throws IOException {
        StringWriter out = new StringWriter();
        copy(in, out);
        return out.toString();
    }

    /**
     * Copy the contents of the given Reader to the given Writer.
     * Closes both when done.
     *
     * @param in  the Reader to copy from
     * @param out the Writer to copy to
     * @throws IOException in case of I/O errors
     */
    private static void copy(Reader in, Writer out) throws IOException {
        try {
            char[] buffer = new char[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                //Ignore
            }
            try {
                out.close();
            } catch (IOException ex) {
                //Ignore
            }
        }
    }

    /**
     * @return The filename of this resource.
     */
    public String getFilename() {
        return location.substring(location.lastIndexOf("/"));
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
