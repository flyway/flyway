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

import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public class PeekingReader extends FilterReader {
    private int[] peekBuffer = new int[256];
    private int peekMax = 0;
    private int peekBufferOffset = 0;
    private final boolean supportsPeekingMultipleLines;

    PeekingReader(final Reader in, final boolean supportsPeekingMultipleLines) {
        super(in);
        this.supportsPeekingMultipleLines = supportsPeekingMultipleLines;
    }

    @Override
    public int read() throws IOException {
        peekBufferOffset++;
        return super.read();
    }

    /**
     * Swallows the next character.
     */
    public void swallow() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        read();
    }

    /**
     * Swallows the next n characters.
     */
    public void swallow(final int n) throws IOException {
        for (int i = 0; i < n; i++) {
            //noinspection ResultOfMethodCallIgnored
            read();
        }
    }

    private int peek() throws IOException {
        if (peekBufferOffset >= peekMax) {
            refillPeekBuffer();
        }

        return peekBuffer[peekBufferOffset];
    }

    private void refillPeekBuffer() throws IOException {
        mark(peekBuffer.length);
        peekMax = peekBuffer.length;
        peekBufferOffset = 0;
        for (int i = 0; i < peekBuffer.length; i++) {
            final int read = super.read();
            peekBuffer[i] = read;
            if (!supportsPeekingMultipleLines && read == '\n') {
                peekMax = i;
                break;
            }
        }
        reset();
    }

    /**
     * Peek ahead in the stream to see if the next character matches this one.
     *
     * @param c The character to match.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean peek(final char c) throws IOException {
        final int r = peek();
        return r != -1 && c == (char) r;
    }

    /**
     * Peek ahead in the stream to see if the next character matches either of these.
     *
     * @param c1 The first character to match.
     * @param c2 The second character to match.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean peek(final char c1, final char c2) throws IOException {
        final int r = peek();
        return r != -1 && (c1 == (char) r || c2 == (char) r);
    }

    /**
     * Peek ahead in the stream to see if the next character is numeric.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekNumeric() throws IOException {
        final int r = peek();
        return isNumeric(r);
    }

    private boolean isNumeric(final int r) {
        return r != -1 && (char) r >= '0' && (char) r <= '9';
    }

    /**
     * Peek ahead in the stream to see if the next character is whitespace.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekWhitespace() throws IOException {
        final int r = peek();
        return isWhitespace(r);
    }

    private boolean isWhitespace(final int r) {
        return r != -1 && Character.isWhitespace((char) r);
    }

    /**
     * Peek ahead in the stream to see if the next character could be a character part of a keyword or identifier.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekKeywordPart(final ParserContext context) throws IOException {
        final int r = peek();
        return isKeywordPart(r, context);
    }

    private boolean isKeywordPart(final int r, final ParserContext context) {
        return r != -1 && ((char) r == '_'
            || (char) r == '$'
            || Character.isLetterOrDigit((char) r)
            || context.isLetter((char) r));
    }

    public boolean peekIgnoreCase(final String str) throws IOException {
        return str.equalsIgnoreCase(peek(str.length()));
    }

    /**
     * Peek ahead in the stream to see if the next characters match this string exactly.
     *
     * @param str The string to match.
     * @return {@code true} if they do, {@code false} if not.
     */
    public boolean peek(final String str) throws IOException {
        return str.equals(peek(str.length()));
    }

    /**
     * Peek ahead in the stream to look at this number of characters ahead in the reader.
     *
     * @param numChars The number of characters.
     * @return The characters.
     */
    public String peek(final int numChars) throws IOException {
        return peek(numChars, false);
    }

    /**
     * Peek ahead in the stream to look at this number of characters ahead in the reader.
     *
     * @param numChars          The number of characters.
     * @param peekMultipleLines Whether the peek should go across lines or not
     * @return The characters.
     */
    public String peek(final int numChars, final boolean peekMultipleLines) throws IOException {
        // If we need to peek beyond the physical size of the peek buffer - eg. we have encountered a very
        // long string literal - then expand the buffer to be big enough to contain it.
        if (numChars >= peekBuffer.length) {
            resizePeekBuffer(numChars);
        }

        if (peekBufferOffset + numChars >= peekMax) {
            refillPeekBuffer();
        }

        final StringBuilder result = new StringBuilder();
        int prevR = -1;
        for (int i = 0; i < numChars; i++) {
            final int r = peekBuffer[peekBufferOffset + i];
            if (r == -1) {
                break;
            } else if (peekBufferOffset + i > peekMax) {
                break;
            } else if (!peekMultipleLines && prevR == '\n') {
                break;
            }
            result.append((char) r);

            prevR = r;
        }
        if (result.length() == 0) {
            return null;
        }
        return result.toString();
    }

    private void resizePeekBuffer(final int newSize) {
        peekBuffer = Arrays.copyOf(peekBuffer, newSize + peekBufferOffset);
    }

    /**
     * Swallows all characters in this stream until any of these delimiting characters has been encountered.
     *
     * @param delimiter1 The first delimiting character.
     * @param delimiter2 The second delimiting character.
     */
    public void swallowUntilExcluding(final char delimiter1, final char delimiter2) throws IOException {
        do {
            if (peek(delimiter1, delimiter2)) {
                break;
            }
            final int r = read();
            if (r == -1) {
                break;
            }
        } while (true);
    }

    /**
     * Reads all characters in this stream until any of these delimiting characters has been encountered.
     *
     * @param delimiter1 The first delimiting character.
     * @param delimiter2 The second delimiting character.
     * @return The string read, without the delimiting characters.
     */
    public String readUntilExcluding(final char delimiter1, final char delimiter2) throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            if (peek(delimiter1, delimiter2)) {
                break;
            }
            final int r = read();
            if (r == -1) {
                break;
            } else {
                result.append((char) r);
            }
        } while (true);
        return result.toString();
    }

    /**
     * Swallows all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter  The delimiting character.
     * @param selfEscape Whether the delimiter can escape itself by being present twice.
     */
    public void swallowUntilIncludingWithEscape(final char delimiter, final boolean selfEscape) throws IOException {
        swallowUntilIncludingWithEscape(delimiter, selfEscape, (char) 0);
    }

    /**
     * Swallows all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter  The delimiting character.
     * @param selfEscape Whether the delimiter can escape itself by being present twice.
     * @param escape     A separate escape character.
     */
    public void swallowUntilIncludingWithEscape(final char delimiter, final boolean selfEscape, final char escape)
        throws IOException {
        do {
            final int r = read();
            if (r == -1) {
                break;
            }
            final char c = (char) r;
            if (escape != 0 && c == escape) {
                swallow();
                continue;
            }
            if (c == delimiter) {
                if (selfEscape && peek(delimiter)) {
                    swallow();
                    continue;
                }
                break;
            }
        } while (true);
    }

    /**
     * Reads all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter  The delimiting character.
     * @param selfEscape Whether the delimiter can escape itself by being present twice.
     * @return The string read, without the delimiting character.
     */
    public String readUntilExcludingWithEscape(final char delimiter, final boolean selfEscape) throws IOException {
        return readUntilExcludingWithEscape(delimiter, selfEscape, (char) 0);
    }

    /**
     * Reads all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter  The delimiting character.
     * @param selfEscape Whether the delimiter can escape itself by being present twice.
     * @param escape     A separate escape character.
     * @return The string read, without the delimiting character.
     */
    public String readUntilExcludingWithEscape(final char delimiter, final boolean selfEscape, final char escape)
        throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            final int r = read();
            if (r == -1) {
                break;
            }
            final char c = (char) r;
            if (escape != 0 && c == escape) {
                final int r2 = read();
                if (r2 == -1) {
                    result.append(escape);
                    break;
                }
                final char c2 = (char) r2;
                result.append(c2);
                continue;
            }
            if (c == delimiter) {
                if (selfEscape && peek(delimiter)) {
                    result.append(delimiter);
                    result.append(delimiter);
                    read();
                    continue;
                }
                break;
            }
            result.append(c);
        } while (true);
        return result.toString();
    }

    /**
     * Swallows all characters in this stream until this delimiting string has been encountered.
     *
     * @param str The delimiting string.
     */
    public void swallowUntilExcluding(final String str) throws IOException {
        do {
            if (peek(str)) {
                break;
            }
            final int r = read();
            if (r == -1) {
                break;
            }
        } while (true);
    }

    /**
     * Reads all characters in this stream until any of the delimiting strings is encountered.
     *
     * @param strings The delimiting strings.
     * @return The string read, without the delimiting string.
     */
    public String readUntilExcluding(String... strings) throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            for (final String str : strings) {
                if (peek(str)) {
                    return result.toString();
                }
            }
            final int r = read();
            if (r == -1) {
                break;
            } else {
                result.append((char) r);
            }
        } while (true);
        return result.toString();
    }

    /**
     * Reads all characters in this stream until any of this delimiting character has been encountered.
     *
     * @param delimiter The delimiting character.
     * @return The string read, including the delimiting characters.
     */
    public String readUntilIncluding(final char delimiter) throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            final int r = read();
            if (r == -1) {
                break;
            }
            final char c = (char) r;
            result.append(c);
            if (c == delimiter) {
                break;
            }
        } while (true);
        return result.toString();
    }

    /**
     * Reads all characters in this stream as long as they can be part of a keyword.
     *
     * @param delimiter The current delimiter.
     * @return The string read.
     */
    public String readKeywordPart(final Delimiter delimiter, final ParserContext context) throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            final boolean isDelimiter = delimiter != null
                && (result.length() == 0 || !delimiter.shouldBeAloneOnLine())
                && peek(delimiter.getDelimiter());

            final boolean shouldAppend = !isDelimiter && peekKeywordPart(context);

            if (shouldAppend) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }

    /**
     * Reads all characters in this stream as long as they can be part of a numeric constant.
     *
     * @return The string read.
     */
    public String readNumeric() throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            if (peekNumeric()) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }

    /**
     * Reads all characters in this stream as long as they are whitespace.
     *
     * @return The string read.
     */
    public String readWhitespace() throws IOException {
        final StringBuilder result = new StringBuilder();
        do {
            if (peekWhitespace()) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }
}
