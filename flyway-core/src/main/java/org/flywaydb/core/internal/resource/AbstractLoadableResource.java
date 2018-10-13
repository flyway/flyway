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
package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.line.LineReader;

import java.io.IOException;
import java.util.zip.CRC32;

public abstract class AbstractLoadableResource implements LoadableResource {
    private Integer checksum;

    @Override
    public final int checksum() {
        if (checksum == null) {
            final CRC32 crc32 = new CRC32();

            LineReader lineReader = null;
            try {
                lineReader = loadAsString();
                Line line;
                while ((line = lineReader.readLine()) != null) {
                    //noinspection Since15
                    crc32.update(StringUtils.trimLineBreak(line.getLine()).getBytes("UTF-8"));
                }
            } catch (IOException e) {
                throw new FlywayException("Unable to calculate checksum for " + getAbsolutePath()
                        + " (" + getAbsolutePathOnDisk() + "): " + e.getMessage(), e);
            } finally {
                IOUtils.close(lineReader);
            }

            checksum = (int) crc32.getValue();
        }
        return checksum;
    }
}