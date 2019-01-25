/*
 * Copyright 2010-2019 Boxfuse GmbH
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

public class PeekingReader extends FilterReader {
    PeekingReader(Reader in) {
        super(in);
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

    /**
     * Peek ahead in the stream to see if the next character matches this one.
     *
     * @param c The character to match.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean peek(char c) throws IOException {
        mark(1);
        int r = read();
        boolean matches = r != -1 && c == (char) r;
        reset();
        return matches;
    }

    /**
     * Peek ahead in the stream to see if the next character matches either of these.
     *
     * @param c1 The first character to match.
     * @param c2 The second character to match.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean peek(char c1, char c2) throws IOException {
        mark(1);
        int r = read();
        boolean matches = r != -1 && (c1 == (char) r || c2 == (char) r);
        reset();
        return matches;
    }

    /**
     * Peek ahead in the stream to see if the next character is numeric.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekNumeric() throws IOException {
        mark(1);
        int r = read();
        boolean matches = r != -1 && (char) r >= '0' && (char) r <= '9';
        reset();
        return matches;
    }

    /**
     * Peek ahead in the stream to see if the next character is whitespace.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekWhitespace() throws IOException {
        mark(1);
        int r = read();
        boolean matches = r != -1 && Character.isWhitespace((char) r);
        reset();
        return matches;
    }

    /**
     * Peek ahead in the stream to see if the next character could be a character part of a keyword or identifier.
     *
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean peekKeywordPart() throws IOException {
        mark(1);
        int r = read();
        boolean matches = r != -1 && ((char) r == '_' || (char) r == '$' || Character.isLetterOrDigit((char) r));
        reset();
        return matches;
    }

    /**
     * Peek ahead in the stream to see if the next characters match this string exactly.
     *
     * @param str The string to match.
     * @return {@code true} if they do, {@code false} if not.
     */
    public boolean peek(String str) throws IOException {
        mark(str.length());
        for (int i = 0; i < str.length(); i++) {
            if (read() != str.charAt(i)) {
                reset();
                return false;
            }
        }
        reset();
        return true;
    }

    /**
     * Peek ahead in the stream to look at this number of characters ahead in the reader.
     *
     * @param numChars The number of characters.
     * @return The characters.
     */
    public String peek(int numChars) throws IOException {
        StringBuilder result = new StringBuilder();
        mark(numChars);
        for (int i = 0; i < numChars; i++) {
            int r = read();
            if (r == -1) {
                break;
            }
            result.append((char) r);
        }
        reset();
        if (result.length() == 0) {
            return null;
        }
        return result.toString();
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
     * @param delimiter    The delimiting character.
     * @param selfEscape   Whether the delimiter can escape itself by being present twice.
     */
    public void swallowUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
        swallowUntilExcludingWithEscape(delimiter, selfEscape, (char) 0, false);
    }

    /**
     * Swallows all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter    The delimiting character.
     * @param selfEscape   Whether the delimiter can escape itself by being present twice.
     * @param escape       A separate escape character.
     * @param escapeEscape Whether the escape character can be used to escape itself.
     */
    public void swallowUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape, boolean escapeEscape) throws IOException {
        String escapedSelf = "" + delimiter + delimiter;
        String escapedDelimiter = "" + escape + delimiter;
        String escapedEscape = "" + escape + escape;

        do {
            if (escapeEscape && peek(escapedEscape)) {
                swallow(escapedEscape.length());
                continue;
            }

            if (selfEscape && peek(escapedSelf)) {
                swallow(escapedSelf.length());
                continue;
            }

            if (peek(escapedDelimiter)) {
                swallow(escapedDelimiter.length());
                continue;
            }

            int r = read();
            if (r == -1 || delimiter == (char) r) {
                break;
            }
        } while (true);
    }

    /**
     * Reads all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter    The delimiting character.
     * @param selfEscape   Whether the delimiter can escape itself by being present twice.
     * @return The string read, without the delimiting character.
     */
    public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
        return readUntilExcludingWithEscape(delimiter, selfEscape, (char) 0, false);
    }

    /**
     * Reads all characters in this stream until this delimiting character has been encountered, taking into account
     * this escape character for the delimiting character.
     *
     * @param delimiter    The delimiting character.
     * @param selfEscape   Whether the delimiter can escape itself by being present twice.
     * @param escape       A separate escape character.
     * @param escapeEscape Whether the escape character can be used to escape itself.
     * @return The string read, without the delimiting character.
     */
    public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape, boolean escapeEscape) throws IOException {
        String escapedSelf = "" + delimiter + delimiter;
        String escapedDelimiter = "" + escape + delimiter;
        String escapedEscape = "" + escape + escape;

        StringBuilder result = new StringBuilder();
        do {
            if (escapeEscape && peek(escapedEscape)) {
                swallow(escapedEscape.length());
                result.append(escape);
                continue;
            }

            if (selfEscape && peek(escapedSelf)) {
                swallow(escapedSelf.length());
                result.append(delimiter);
                continue;
            }

            if (peek(escapedDelimiter)) {
                swallow(escapedDelimiter.length());
                result.append(delimiter);
                continue;
            }

            int r = read();
            if (r == -1 || delimiter == (char) r) {
                break;
            } else {
                result.append((char) r);
            }
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
     * Reads all characters in this stream as long as they can be part of a keyword.
     *
     * @return The string read.
     */
    public String readKeywordPart() throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peekKeywordPart()) {
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