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
package org.flywaydb.core.internal.resource.filesystem;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.util.BomStrippingReader;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;

/**
 * A resource on the filesystem.
 */
public class FileSystemResource extends LoadableResource {

    private static final Log LOG = LogFactory.getLog(FileSystemResource.class);









    /**
     * The location of the resource on the filesystem.
     */
    private final File file;
    private final String relativePath;
    private final Charset encoding;




    /**
     * Creates a new ClassPathResource.
     *
     * @param fileNameWithPath The path and filename of the resource on the filesystem.
     */
    public FileSystemResource(Location location, String fileNameWithPath, Charset encoding



    ) {
        this.file = new File(new File(fileNameWithPath).getPath());
        this.relativePath = location == null ? file.getPath() : location.getPathRelativeToThis(file.getPath()).replace("\\", "/");
        this.encoding = encoding;



    }

    /**
     * @return The location of the resource on the filesystem.
     */
    @Override
    public String getAbsolutePath() {
        return file.getPath();
    }

    /**
     * Retrieves the location of this resource on disk.
     *
     * @return The location of this resource on disk.
     */
    @Override
    public String getAbsolutePathOnDisk() {
        return file.getAbsolutePath();
    }

    @Override
    public Reader read() {
        try {
            return Channels.newReader(FileChannel.open(file.toPath(), StandardOpenOption.READ), encoding.newDecoder(), 4096);
        } catch (IOException e){
            LOG.debug("Unable to load filesystem resource" + file.getPath() + " using FileChannel.open." +
                    " Falling back to FileInputStream implementation. Exception message: " + e.getMessage());
        }

        try {
            return new BufferedReader(new BomStrippingReader(new InputStreamReader(new FileInputStream(file), encoding)));
        } catch (IOException e) {
            throw new FlywayException("Unable to load filesystem resource: " + file.getPath() + " (encoding: " + encoding + ")", e);
        }
    }












    /**
     * @return The filename of this resource, without the path.
     */
    @Override
    public String getFilename() {
        return file.getName();
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }
}