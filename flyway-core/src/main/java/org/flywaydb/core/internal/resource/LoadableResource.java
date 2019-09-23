/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.BomFilter;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * A loadable resource.
 */
public abstract class LoadableResource implements Resource, Comparable<LoadableResource> {

    private Integer checksum;

    /**
     * Reads the contents of this resource.
     *
     * @return The reader with the contents of the resource.
     */
    public abstract Reader read();











    /**
     * Calculates the checksum of this resource. The checksum is encoding and line-ending independent.
     *
     * @return The crc-32 checksum of the bytes.
     */
    public final int checksum() {
        if (checksum == null) {
            final CRC32 crc32 = new CRC32();

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(read(), 4096);

                String line = reader.readLine();

                if (line != null) {
                    line = BomFilter.FilterBomFromString(line);

                    do {
                        //noinspection Since15
                        crc32.update(StringUtils.trimLineBreak(line).getBytes(StandardCharsets.UTF_8));
                    } while ((line = reader.readLine()) != null);
                }
            } catch (IOException e) {
                throw new FlywayException("Unable to calculate checksum for " + getFilename() + ": " + e.getMessage(), e);
            } finally {
                IOUtils.close(reader);
            }

            checksum = (int) crc32.getValue();
        }
        return checksum;
    }

    @Override
    public int compareTo(LoadableResource o) {
        return getRelativePath().compareTo(o.getRelativePath());
    }
}