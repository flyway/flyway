/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.resource.filesystem;

import static org.flywaydb.core.internal.util.DataUnits.MEGABYTE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.util.BomStrippingReader;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

@CustomLog
public class FileSystemResource extends LoadableResource {
    /**
     * The minimum file size (in bytes) as of which to apply streaming, when active. Files smaller than this threshold
     * will not be streamed, regardless of the actual setting.
     */
    private static final long STREAM_MINIMUM_THRESHOLD = MEGABYTE.toBytes(1);
    private final boolean stream;
    private final File file;
    private final String relativePath;
    private final Charset encoding;
    private final boolean detectEncoding;

    public FileSystemResource(final Location location,
        final String fileNameWithPath,
        final Charset encoding,
        final boolean stream) {
        this(location, fileNameWithPath, encoding, false, stream);
    }

    public FileSystemResource(final Location location,
        final String fileNameWithPath,
        final Charset encoding,
        final boolean detectEncoding,
        final boolean stream) {
        this.file = new File(new File(fileNameWithPath).getPath());
        this.relativePath = location == null
            ? file.getPath()
            : location.getPathRelativeToThis(file.getPath()).replace("\\", "/");
        this.encoding = encoding;
        this.detectEncoding = detectEncoding;
        this.stream = stream;
    }

    @Override
    public String getAbsolutePath() {
        return file.getPath();
    }

    @Override
    public String getAbsolutePathOnDisk() {
        return file.getAbsolutePath();
    }

    @Override
    public Reader read() {
        Charset charSet = encoding;
        if (detectEncoding) {
            try {
                charSet = EncodingDetector.detectFileEncoding(file.toPath());
            } catch (final FlywayEncodingDetectionException e) {
                LOG.warn("Could not detect file encoding: "
                    + e.getMessage()
                    + "\nThis may cause issues with your deployments."
                    + " We recommend using a consistent and supported encoding for all your files. See "
                    + FlywayDbWebsiteLinks.FILE_ENCODING_HELP);
            }
        }
        try {
            return Channels.newReader(FileChannel.open(file.toPath(), StandardOpenOption.READ),
                charSet.newDecoder(),
                4096);
        } catch (final IOException e) {
            LOG.debug("Unable to load filesystem resource"
                + file.getPath()
                + " using FileChannel.open."
                + " Falling back to FileInputStream implementation. Exception message: "
                + e.getMessage());
        }

        try {
            return new BufferedReader(new BomStrippingReader(new InputStreamReader(new FileInputStream(file),
                charSet)));
        } catch (final IOException e) {
            throw new FlywayException("Unable to load filesystem resource: "
                + file.getPath()
                + " (encoding: "
                + charSet
                + ")", e);
        }
    }

    @Override
    public boolean shouldStream() {
        return stream && file.length() > STREAM_MINIMUM_THRESHOLD;
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
