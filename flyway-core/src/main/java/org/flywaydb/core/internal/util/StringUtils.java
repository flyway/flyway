/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various string-related utilities.
 */
public class StringUtils {
    /**
     * Prevents instantiation.
     */
    private StringUtils() {
        // Do nothing.
    }

    /**
     * Trims or pads (with spaces) this string, so it has this exact length.
     *
     * @param str    The string to adjust. {@code null} is treated as an empty string.
     * @param length The exact length to reach.
     * @return The adjusted string.
     */
    public static String trimOrPad(String str, int length) {
        return trimOrPad(str, length, ' ');
    }

    /**
     * Trims or pads this string, so it has this exact length.
     *
     * @param str     The string to adjust. {@code null} is treated as an empty string.
     * @param length  The exact length to reach.
     * @param padChar The padding character.
     * @return The adjusted string.
     */
    public static String trimOrPad(String str, int length, char padChar) {
        String result;
        if (str == null) {
            result = "";
        } else {
            result = str;
        }

        if (result.length() > length) {
            return result.substring(0, length);
        }

        while (result.length() < length) {
            result += padChar;
        }
        return result;
    }

    /**
     * <p>Checks if the String contains only unicode digits. A decimal point is not a unicode digit and returns
     * false.</p> <p/> <p>{@code null} will return {@code false}. An empty String ("") will return {@code true}.</p>
     * <p/>
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = true
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * </pre>
     *
     * @param str the String to check, may be null
     * @return {@code true} if only contains digits, and is non-null
     */
    public static boolean isNumeric(String str) {
        return str != null && str.matches("\\d*");
    }

    /**
     * Replaces all sequences of whitespace by a single blank. Ex.: "&nbsp;&nbsp;&nbsp;&nbsp;" -> " "
     *
     * @param str The string to analyse.
     * @return The input string, with all whitespace collapsed.
     */
    public static String collapseWhitespace(String str) {
        return str.replaceAll("\\s+", " ");
    }

    /**
     * Returns the first n characters from this string, where n = count. If the string is shorter, the entire string
     * will be returned. If the string is longer, it will be truncated.
     *
     * @param str   The string to parse.
     * @param count The amount of characters to return.
     * @return The first n characters from this string, where n = count.
     */
    public static String left(String str, int count) {
        if (str == null) {
            return null;
        }

        if (str.length() < count) {
            return str;
        }

        return str.substring(0, count);
    }

    /**
     * Replaces all occurrances of this originalToken in this string with this replacementToken.
     *
     * @param str              The string to process.
     * @param originalToken    The token to replace.
     * @param replacementToken The replacement.
     * @return The transformed str.
     */
    public static String replaceAll(String str, String originalToken, String replacementToken) {
        return str.replaceAll(Pattern.quote(originalToken), Matcher.quoteReplacement(replacementToken));
    }

    /**
     * Checks whether this string is not {@code null} and not <i>empty</i>.
     *
     * @param str The string to check.
     * @return {@code true} if it has content, {@code false} if it is {@code null} or blank.
     */
    public static boolean hasLength(String str) {
        return str != null && str.length() > 0;
    }

    /**
     * Turns this string array in one comma-delimited string.
     *
     * @param strings The array to process.
     * @return The new comma-delimited string. An empty string if {@code strings} is empty. {@code null} if strings is {@code null}.
     */
    public static String arrayToCommaDelimitedString(Object[] strings) {
        if (strings == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(String.valueOf(strings[i]));
        }
        return builder.toString();
    }

    /**
     * Checks whether this string isn't {@code null} and contains at least one non-blank character.
     *
     * @param s The string to check.
     * @return {@code true} if it has text, {@code false} if not.
     */
    public static boolean hasText(String s) {
        return (s != null) && (s.trim().length() > 0);
    }

    /**
     * Splits this string into an array using these delimiters.
     *
     * @param str        The string to split.
     * @param delimiters The delimiters to use.
     * @return The resulting array.
     */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        String[] tokens = str.split("[" + delimiters + "]");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

    /**
     * Counts the number of occurrences of this token in this string.
     *
     * @param str   The string to analyse.
     * @param token The token to look for.
     * @return The number of occurrences.
     */
    public static int countOccurrencesOf(String str, String token) {
        if (str == null || token == null || str.length() == 0 || token.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(token, pos)) != -1) {
            ++count;
            pos = idx + token.length();
        }
        return count;
    }

    /**
     * Replace all occurences of a substring within a string with
     * another string.
     *
     * @param inString   String to examine
     * @param oldPattern String to replace
     * @param newPattern String to insert
     * @return a String with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; // our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        // remember to append any characters to the right of a match
        return sb.toString();
    }

    /**
     * Convenience method to return a Collection as a comma-delimited
     * String. E.g. useful for {@code toString()} implementations.
     *
     * @param collection the Collection to analyse
     * @return The comma-delimited String.
     */
    public static String collectionToCommaDelimitedString(Collection<?> collection) {
        return collectionToDelimitedString(collection, ", ");
    }

    /**
     * Convenience method to return a Collection as a delimited
     * String. E.g. useful for {@code toString()} implementations.
     *
     * @param collection the Collection to analyse
     * @param delimiter  The delimiter.
     * @return The delimited String.
     */
    public static String collectionToDelimitedString(Collection<?> collection, String delimiter) {
        if (collection == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Trim leading whitespace from the given String.
     *
     * @param str the String to check
     * @return the trimmed String
     * @see java.lang.Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

    /**
     * Trim trailing whitespace from the given String.
     *
     * @param str the String to check
     * @return the trimmed String
     * @see java.lang.Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
