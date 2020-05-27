/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public class PeekingReader extends FilterReader {
    private int[] peekBuffer = new int[256];
    private int peekMax = 0;
    private int peekBufferOffset = 0;

    PeekingReader(Reader in) {
        super(in);
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
    public void swallow(int n) throws IOException {
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
            int read = super.read();
            peekBuffer[i] = read;
            if (read == '\n') {
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
    public boolean peek(char c) throws IOException {
        int r = peek();
        return r != -1 && c == (char) r;
    }

    /**
     * Peek ahead in the stream to see if the next character matches either of these.
     *
     * @param c1 The first character to match.
     * @param c2 The second character to match.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean peek(char c1, char c2) throws IOException {
        int r = peek();
        return r != -1 && (c1 == (char) r || c2 == (char) r);
    }

    /**
     * Peek ahead in the stream to see if the next character is numeric.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekNumeric() throws IOException {
        int r = peek();
        return isNumeric(r);
    }

    private boolean isNumeric(int r) {
        return r != -1 && (char) r >= '0' && (char) r <= '9';
    }

    /**
     * Peek ahead in the stream to see if the next character is whitespace.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekWhitespace() throws IOException {
        int r = peek();
        return isWhitespace(r);
    }

    private boolean isWhitespace(int r) {
        return r != -1 && Character.isWhitespace((char) r);
    }

    /**
     * Peek ahead in the stream to see if the next character could be a character part of a keyword or identifier.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekKeywordPart(ParserContext context) throws IOException {
        int r = peek();
        return isKeywordPart(r, context);
    }

    private boolean isKeywordPart(int r, ParserContext context) {
        return r != -1 && ((char) r == '_' || (char) r == '$' || Character.isLetterOrDigit((char) r) || context.isLetter((char)r));
    }

    /**
     * Peek ahead in the stream to see if the next characters match this string exactly.
     *
     * @param str The string to match.
     * @return {@code true} if they do, {@code false} if not.
     */
    public boolean peek(String str) throws IOException {
        return str.equals(peek(str.length()));
    }

    /**
     * Peek ahead in the stream to look at this number of characters ahead in the reader.
     *
     * @param numChars The number of characters.
     * @return The characters.
     */
    public String peek(int numChars) throws IOException {
        // If we need to peek beyond the physical size of the peek buffer - eg. we have encountered a very
        // long string literal - then expand the buffer to be big enough to contain it.
        if (numChars >= peekBuffer.length) {
            resizePeekBuffer(numChars);
        }

        if (peekBufferOffset + numChars >= peekMax) {
            refillPeekBuffer();
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numChars; i++) {
            int r = peekBuffer[peekBufferOffset + i];
            if (r == -1) {
                break;
            } else if (peekBufferOffset + i > peekMax) {
                break;
            }
            result.append((char) r);
        }
        if (result.length() == 0) {
            return null;
        }
        return result.toString();
    }

    /**
     * Return the next non-whitespace character
     * @return The character
     */
    public char peekNextNonWhitespace() throws IOException {
        int i = 1;
        String c = peek(i++);
        while (c.trim().isEmpty()) {
            c = peek(i++);
        }

        return c.charAt(c.length()-1);
    }

    private void resizePeekBuffer(int newSize) {
        peekBuffer = Arrays.copyOf(peekBuffer, newSize + peekBufferOffset);
    }

    /**
     * Swallows all characters in this stream until any of these delimiting characters has been encountered.
     *
     * @param delimiter1 The first delimiting character.
     * @param delimiter2 The second delimiting character.
     */
    public void swallowUntilExcluding(char delimiter1, char delimiter2) throws IOException {
        do {
            if (peek(delimiter1, delimiter2)) {
                break;
            }
            int r = read();
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
    public String readUntilExcluding(char delimiter1, char delimiter2) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peek(delimiter1, delimiter2)) {
                break;
            }
            int r = read();
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
    public void swallowUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
        swallowUntilExcludingWithEscape(delimiter, selfEscape, (char) 0);
    }

    /**
     * Swallows all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter  The delimiting character.
     * @param selfEscape Whether the delimiter can escape itself by being present twice.
     * @param escape     A separate escape character.
     */
    public void swallowUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape) throws IOException {
        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;
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
    public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
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
    public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;
            if (escape != 0 && c == escape) {
                int r2 = read();
                if (r2 == -1) {
                    result.append(escape);
                    break;
                }
                char c2 = (char) r2;
                result.append(c2);
                continue;
            }
            if (c == delimiter) {
                if (selfEscape && peek(delimiter)) {
                    result.append(delimiter);
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
    public void swallowUntilExcluding(String str) throws IOException {
        do {
            if (peek(str)) {
                break;
            }
            int r = read();
            if (r == -1) {
                break;
            }
        } while (true);
    }

    /**
     * Reads all characters in this stream until this delimiting string has been encountered.
     *
     * @param str The delimiting string.
     * @return The string read, without the delimiting string.
     */
    public String readUntilExcluding(String str) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peek(str)) {
                break;
            }
            int r = read();
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
    public String readUntilIncluding(char delimiter) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;
            result.append(c);
            if (c == delimiter) {
                break;
            }
        } while (true);
        return result.toString();
    }

    /**
     * Reads all characters in this stream until the delimiting sequence is encountered.
     *
     * @param delimiterSequence The delimiting sequence.
     * @return The string read, including the delimiting characters.
     */
    public String readUntilIncluding(String delimiterSequence) throws IOException {
        StringBuilder result = new StringBuilder();

        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;

            result.append(c);
            if (result.toString().endsWith(delimiterSequence)) {
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
    public String readKeywordPart(Delimiter delimiter, ParserContext context) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if ((delimiter == null || !peek(delimiter.getDelimiter())) && peekKeywordPart(context)) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }

    /**
     * Swallows all characters in this stream as long as they can be part of a numeric constant.
     */
    public void swallowNumeric() throws IOException {
        do {
            if (!peekNumeric()) {
                return;
            }
            swallow();
        } while (true);
    }

    /**
     * Reads all characters in this stream as long as they can be part of a numeric constant.
     *
     * @return The string read.
     */
    public String readNumeric() throws IOException {
        StringBuilder result = new StringBuilder();
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
        StringBuilder result = new StringBuilder();
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