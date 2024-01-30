package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class RecordingReader extends FilterReader {
    private boolean paused;
    private final Recorder recorder;

    RecordingReader(Recorder recorder, Reader in) {
        super(in);
        this.recorder = recorder;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1 && !paused) {
            recorder.record((char) read);
        }
        return read;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        paused = true;
        super.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        paused = false;
    }
}