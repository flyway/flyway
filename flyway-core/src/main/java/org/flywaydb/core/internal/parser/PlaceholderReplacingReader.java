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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderReplacingReader extends FilterReader {
    private final String prefix;
    private final String suffix;
    private final Map<String, String> placeholders;

    /**
     * The number of chars by which to increase the read-ahead limit to factor in the difference in length between
     * placeholders (with prefix and suffix) and their replacements.
     */
    private final int readAheadLimitAdjustment;

    private final StringBuilder buffer = new StringBuilder();
    private String markBuffer;

    private String replacement;
    private int replacementPos;

    private String markReplacement;
    private int markReplacementPos;

    public PlaceholderReplacingReader(String prefix, String suffix, Map<String, String> placeholders, Reader in) {
        super(in);
        this.prefix = prefix;
        this.suffix = suffix;
        this.placeholders = placeholders;

        int prefixSuffixLength = prefix.length() + suffix.length();

        // As the readers using this will read until they get the necessary characters, this reader needs to assume the
        // worst case scenario (all the placeholders being sequential) when taking into account how much further ahead
        // it needs to be adjust the mark
        int placeholderSizeDifferenceTotal = 0;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            int placeholderLength = prefixSuffixLength + entry.getKey().length();
            int replacementLength = (entry.getValue() != null) ? entry.getValue().length() : 0;

            placeholderSizeDifferenceTotal += Math.max(0, placeholderLength - replacementLength);
        }
        readAheadLimitAdjustment = placeholderSizeDifferenceTotal;
    }

    public static PlaceholderReplacingReader create(Configuration configuration, ParsingContext parsingContext, Reader reader) {
        Map<String, String> placeholders = new HashMap<>();
        Map<String, String> configurationPlaceholders = configuration.getPlaceholders();
        Map<String, String> parsingContextPlaceholders = parsingContext.getPlaceholders();

        placeholders.putAll(configurationPlaceholders);
        placeholders.putAll(parsingContextPlaceholders);

        return new PlaceholderReplacingReader(
                configuration.getPlaceholderPrefix(),
                configuration.getPlaceholderSuffix(),
                placeholders,
                reader);
    }

    @Override
    public int read() throws IOException {
        if (replacement == null) {

            // if we have a previous read, then consume it
            if (buffer.length() > 0) {
                char c = buffer.charAt(0);
                buffer.deleteCharAt(0);
                return c;
            }

            // else read ahead by the prefix length
            int r;
            do {
                r = super.read();
                if (r == -1) {
                    break;
                }

                buffer.append((char) r);
            } while (buffer.length() < prefix.length() && endsWith(buffer, prefix.substring(0, buffer.length())));

            // if the buffer does not contain the prefix
            if (!endsWith(buffer, prefix)) {
                // if it contain data, return the first character of it
                if (buffer.length() > 0) {
                    char c = buffer.charAt(0);
                    buffer.deleteCharAt(0);
                    return c;
                }
                // else return -1
                return -1;
            }
            // if the buffer contained the prefix, wipe the buffer
            buffer.delete(0, buffer.length());

            // begin reading ahead until we get to the suffix
            StringBuilder placeholderBuilder = new StringBuilder();
            do {
                int r1 = super.read();
                if (r1 == -1) {
                    break;
                } else {
                    placeholderBuilder.append((char) r1);
                }
            } while (!endsWith(placeholderBuilder, suffix));

            // delete the suffix from the builder
            for (int i = 0; i < suffix.length(); i++) {
                placeholderBuilder.deleteCharAt(placeholderBuilder.length() - 1);
            }

            // look up the placeholder string
            String placeholder = placeholderBuilder.toString();
            if (!placeholders.containsKey(placeholder)) {
                String canonicalPlaceholder = prefix + placeholder + suffix;

                if (placeholder.contains("flyway:")) {
                    throw new FlywayException("Failed to populate value for default placeholder: "
                            + canonicalPlaceholder);
                }

                throw new FlywayException("No value provided for placeholder: "
                        + canonicalPlaceholder
                        + ".  Check your configuration!");
            }

            // set the current placeholder replacement
            replacement = placeholders.get(placeholder);

            // Empty placeholder value -> move to the next character
            if (replacement == null || replacement.length() == 0) {
                replacement = null;
                return read();
            }
        }

        int result = replacement.charAt(replacementPos);
        replacementPos++;
        if (replacementPos >= replacement.length()) {
            replacement = null;
            replacementPos = 0;
        }
        return result;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int count = 0;
        for (int i = 0; i < len; i++) {
            int r = read();
            if (r == -1) {
                return count == 0 ? -1 : count;
            }
            cbuf[off + i] = (char) r;
            count++;
        }
        return count;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        markBuffer = buffer.toString();
        markReplacement = replacement;
        markReplacementPos = replacementPos;
        super.mark(readAheadLimit + readAheadLimitAdjustment);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        buffer.delete(0, buffer.length());
        buffer.append(markBuffer);
        replacement = markReplacement;
        replacementPos = markReplacementPos;
    }

    private boolean endsWith(StringBuilder result, String str) {
        if (result.length() < str.length()) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (result.charAt(result.length() - str.length() + i) != str.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}