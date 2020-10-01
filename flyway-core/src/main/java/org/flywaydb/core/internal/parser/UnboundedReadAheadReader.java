/*
 * Copyright © Red Gate Software Ltd 2010-2020
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

    protected UnboundedReadAheadReader(Reader in) {
        super(in);
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
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

        int read = in.read();
        if (markIndex != -1) {
            setValue(read);
            readIndex++;
            currentBuffersSize++;
        }
        return read;
    }

    private int getValue(int index) {
        int buffersIndex = index / bufferSize;
        int buffersOffset = index - (buffersIndex * bufferSize);
        return buffers.get(buffersIndex)[buffersOffset];
    }

    private void setValue(int value) {
        int buffersIndex = readIndex / bufferSize;
        int buffersOffset = readIndex - (buffersIndex * bufferSize);

        if (buffersOffset == 0) {
            buffers.add(new int[bufferSize]);
        }

        buffers.get(buffersIndex)[buffersOffset] = value;
    }

    private void freeBuffers() {
        int buffersToRemove = (markIndex / bufferSize) - 1;

        for (int i = 0; i < buffersToRemove; i++) {
            buffers.remove(0);
            readIndex -= bufferSize;
            currentBuffersSize -= bufferSize;
        }
    }
}