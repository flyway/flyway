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
package org.flywaydb.core.internal.line;

import org.flywaydb.core.api.FlywayException;

import java.io.IOException;
import java.io.Reader;

public class DefaultLineReader implements LineReader {
    private final Reader reader;

    private int lineNumber;

    public DefaultLineReader(Reader reader) {
        this.reader = new BufferedCharReader(reader);
    }

    @Override
    public Line readLine() {
        lineNumber++;
        try {
            StringBuilder line = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                char ch = (char) c;
                line.append(ch);
                if (ch == '\n') {
                    break;
                }
            }
            if (line.length() == 0) {
                return null;
            }
            return new DefaultLine(lineNumber, line.toString());
        } catch (IOException e) {
            throw new FlywayException("Unable to read line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private static class BufferedCharReader extends Reader {
        private final Reader r;
        private final char[] buffer = new char[16384];
        private boolean end;
        private int max = 0;
        private int pos = 0;

        private BufferedCharReader(Reader r) {
            this.r = r;
        }

        @Override
        public int read() throws IOException {
            if (pos < max) {
                return buffer[pos++];
            }
            if (end) {
                return -1;
            }
            max = r.read(buffer);
            if (max <= 0) {
                end = true;
                return -1;
            }
            pos = 0;
            return buffer[pos++];
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return r.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            r.close();
        }
    }
}