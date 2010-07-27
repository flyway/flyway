/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * Utility class for dealing with classpath resources.
 */
public class ResourceUtils {

    /**
     * The default buffer size to use.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Prevents instantiation.
     */
    private ResourceUtils() {
        //Do nothing
    }

    /**
     * Loads the resource at this location within the classpath in a string using UTF-8 encoding.
     *
     * @param location The location of the resource on the classpath.
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(String location) {
        return loadResourceAsString(new ClassPathResource(location), "UTF-8", null);
    }

    /**
     * Loads this resource in a string using this encoding.
     *
     * @param resource The resource to load.
     * @param encoding The encoding of the resource.
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(Resource resource, String encoding) {
        return loadResourceAsString(resource, encoding, null);
    }

    /**
     * Loads this resource in a string using this encoding and optionally calculates a CRC-32 checksum
     *
     * @param resource The resource to load.
     * @param encoding The encoding of the resource.
     * @param checksum the checksum object to be used, if <code>null</code> no checksum is calculated
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(Resource resource, String encoding, Checksum checksum) {
        InputStream inputStream = null;
        try {
            inputStream = resource.getInputStream();
            final InputStream checkedInputStream;
            if (checksum != null) {
                checkedInputStream = new CheckedInputStream(inputStream, checksum);
            } else {
                checkedInputStream = inputStream;
            }
            Reader reader = new InputStreamReader(checkedInputStream, Charset.forName(encoding));
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load resource: " + resource.getDescription() + " (encoding: " + encoding + ")", e);
        } finally {
            closeQuietly(inputStream);
        }
    }

    /**
     * Computes the checksum of a classpath resource using.
     *
     * @param resource the classpath resource.
     * @return the calculated checksum
     * @throws java.io.IOException      if an IO error occurs reading the resource
     */
    public static long checksum(Resource resource) throws IOException {
        return checksum(resource, new CRC32()).getValue();
    }

    /**
     * Computes the checksum of a classpath resource using the specified checksum object.
     *
     * @param resource the classpath resource.
     * @param checksum the checksum object to be used, must not be <code>null</code>
     * @return the checksum specified, updated with the content of the file
     * @throws java.io.IOException      if an IO error occurs reading the resource
     */
    public static Checksum checksum(Resource resource, Checksum checksum) throws IOException {
        InputStream in = resource.getInputStream();
        try {
            in = new CheckedInputStream(in, checksum);
            copyLarge(in, new NullOutputStream());
        } finally {
            closeQuietly(in);
        }
        return checksum;
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output  the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since Commons IO 1.3
     */
    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Unconditionally close an <code>InputStream</code>.
     * <p>
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param input  the InputStream to close, may be null or already closed
     */
    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
    /**
     * Unconditionally close an <code>Reader</code>.
     * <p>
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param reader  the Reader to close, may be null or already closed
     */
    public static void closeQuietly(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static Long calculateChecksum(String source) {
        final CRC32 crc32 = new CRC32();
        crc32.update(source.getBytes());
        return crc32.getValue();
    }

    private static class NullOutputStream extends OutputStream {

        /**
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        public void write(byte[] b, int off, int len) {
            //to /dev/null
        }

        /**
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b) {
            //to /dev/null
        }

        /**
         * @see java.io.OutputStream#write(byte[])
         */
        public void write(byte[] b) throws IOException {
            //to /dev/null
        }

    }

}
