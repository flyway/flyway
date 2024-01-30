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