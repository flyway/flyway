/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
