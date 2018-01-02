/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util.scanner.filesystem;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A resource on the filesystem.
 */
public class FileSystemResource implements LoadableResource, Comparable<FileSystemResource> {
    /**
     * The location of the resource on the filesystem.
     */
    private File location;

    /**
     * Creates a new ClassPathResource.
     *
     * @param location The location of the resource on the filesystem.
     */
    public FileSystemResource(String location) {
        this.location = new File(location.replace("\\", "/").replace("//", "/"));
    }

    /**
     * @return The location of the resource on the filesystem.
     */
    public String getLocation() {
        return location.getPath().replace("\\", "/");
    }

    /**
     * Retrieves the location of this resource on disk.
     *
     * @return The location of this resource on disk.
     */
    public String getLocationOnDisk() {
        return location.getAbsolutePath();
    }

    /**
     * Loads this resource as a string.
     *
     * @param encoding The encoding to use.
     * @return The string contents of the resource.
     */
    public String loadAsString(String encoding) {
        try {
            InputStream inputStream = new FileInputStream(location);
            Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));

            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new FlywayException("Unable to load filesystem resource: " + location.getPath() + " (encoding: " + encoding + ")", e);
        }
    }

    /**
     * Loads this resource as a byte array.
     *
     * @return The contents of the resource.
     */
    public byte[] loadAsBytes() {
        try {
            InputStream inputStream = new FileInputStream(location);
            return FileCopyUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            throw new FlywayException("Unable to load filesystem resource: " + location.getPath(), e);
        }
    }

    /**
     * @return The filename of this resource, without the path.
     */
    public String getFilename() {
        return location.getName();
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(FileSystemResource o) {
        return location.compareTo(o.location);
    }
}
