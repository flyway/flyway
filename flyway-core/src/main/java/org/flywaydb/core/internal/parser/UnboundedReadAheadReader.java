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
package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class UnboundedReadAheadReader extends FilterReader {

    protected ArrayList<int[]> buffers = new ArrayList<>();

    private int readIndex = 0;
    private int markIndex = -1;
    private int currentBuffersSize = 0;

    private static final int bufferSize = 512;

    protected UnboundedReadAheadReader(final Reader in) {
        super(in);
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        markIndex = readIndex;
    }

    @Override
    public void reset() throws IOException {
        readIndex = markIndex;
        freeBuffers();
        markIndex = -1;
    }

    @Override
    public int read() throws IOException {
        if (readIndex < currentBuffersSize) {
            return getValue(readIndex++);
        }

        final int read = in.read();
        if (markIndex != -1) {
            setValue(read);
            readIndex++;
            currentBuffersSize++;
        }
        return read;
    }

    private int getValue(final int index) {
        final int buffersIndex = index / bufferSize;
        final int buffersOffset = index - (buffersIndex * bufferSize);
        return buffers.get(buffersIndex)[buffersOffset];
    }

    private void setValue(final int value) {
        final int buffersIndex = readIndex / bufferSize;
        final int buffersOffset = readIndex - (buffersIndex * bufferSize);

        if (buffersOffset == 0) {
            buffers.add(new int[bufferSize]);
        }

        buffers.get(buffersIndex)[buffersOffset] = value;
    }

    private void freeBuffers() {
        final int buffersToRemove = (markIndex / bufferSize) - 1;

        for (int i = 0; i < buffersToRemove; i++) {
            buffers.remove(0);
            readIndex -= bufferSize;
            currentBuffersSize -= bufferSize;
        }
    }
}
