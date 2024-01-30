package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class PositionTrackingReader extends FilterReader {
    private final PositionTracker tracker;
    private boolean paused;

    PositionTrackingReader(PositionTracker tracker, Reader in) {
        super(in);
        this.tracker = tracker;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1 && !paused) {
            tracker.nextPos();
            char c = (char) read;
            if (c == '\n') {
                tracker.linefeed();
            } else if (c == '\r') {
                tracker.carriageReturn();
            } else {
                if (!Character.isWhitespace(c)) {
                    tracker.nextColIgnoringWhitespace();
                }
                tracker.nextCol();
            }
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