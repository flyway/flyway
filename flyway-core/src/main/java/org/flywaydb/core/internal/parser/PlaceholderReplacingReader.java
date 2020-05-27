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
        int maxPlaceholderLength = prefixSuffixLength;
        int minReplacementLength = Integer.MAX_VALUE;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            maxPlaceholderLength = Math.max(maxPlaceholderLength, prefixSuffixLength + entry.getKey().length());
            int valueLength = (entry.getValue() != null) ? entry.getValue().length() : 0;
            minReplacementLength = Math.min(minReplacementLength, valueLength);
        }
        readAheadLimitAdjustment = Math.max(maxPlaceholderLength - minReplacementLength, 0);
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
            if (buffer.length() > 0) {
                char c = buffer.charAt(0);
                buffer.deleteCharAt(0);
                return c;
            }

            int r;
            do {
                r = super.read();
                if (r == -1) {
                    break;
                }

                buffer.append((char) r);
            } while (buffer.length() < prefix.length() && endsWith(buffer, prefix.substring(0, buffer.length())));
            if (!endsWith(buffer, prefix)) {
                if (buffer.length() > 0) {
                    char c = buffer.charAt(0);
                    buffer.deleteCharAt(0);
                    return c;
                }
                return -1;
            }
            buffer.delete(0, buffer.length());

            StringBuilder placeholderBuilder = new StringBuilder();
            do {
                int r1 = in.read();
                if (r1 == -1) {
                    break;
                } else {
                    placeholderBuilder.append((char) r1);
                }
            } while (!endsWith(placeholderBuilder, suffix));
            for (int i = 0; i < suffix.length(); i++) {
                placeholderBuilder.deleteCharAt(placeholderBuilder.length() - 1);
            }


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