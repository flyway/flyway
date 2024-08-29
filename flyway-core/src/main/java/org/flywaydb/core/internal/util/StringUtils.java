/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.flywaydb.core.internal.configuration.ConfigUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {
    private static final String WHITESPACE_CHARS = " \t\n\f\r";

    /**
     * Trims or pads (with spaces) this string, so it has this exact length.
     *
     * @param str The string to adjust. {@code null} is treated as an empty string.
     * @param length The exact length to reach.
     * @return The adjusted string.
     */
    public static String trimOrPad(String str, int length) {
        return trimOrPad(str, length, ' ');
    }

    /**
     * Trims or pads this string, so it has this exact length.
     *
     * @param str The string to adjust. {@code null} is treated as an empty string.
     * @param length The exact length to reach.
     * @param padChar The padding character.
     * @return The adjusted string.
     */
    public static String trimOrPad(String str, int length, char padChar) {
        StringBuilder result;
        if (str == null) {
            result = new StringBuilder();
        } else {
            result = new StringBuilder(str);
        }

        if (result.length() > length) {
            return result.substring(0, length);
        }

        while (result.length() < length) {
            result.append(padChar);
        }
        return result.toString();
    }

    /**
     * Trims or pads this string, so it has this exact length.
     *
     * @param str The string to adjust. {@code null} is treated as an empty string.
     * @param length The exact length to reach.
     * @param padChar The padding character.
     * @return The adjusted string.
     */
    public static String trimOrLeftPad(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() > length) {
            return str.substring(0, length);
        }
        return leftPad(str, length, padChar);
    }

    public static String leftPad(String original, int length, char padChar) {
        StringBuilder result = new StringBuilder(original);
        while (result.length() < length) {
            result.insert(0, padChar);
        }
        return result.toString();
    }

    public static String rightPad(String original, int length, char padChar) {
        StringBuilder result = new StringBuilder(original);
        while (result.length() < length) {
            result.append(padChar);
        }
        return result.toString();
    }

    /**
     * Replaces all sequences of whitespace by a single blank. Ex.: "&nbsp;&nbsp;&nbsp;&nbsp;" -> " "
     *
     * @param str The string to analyse.
     * @return The input string, with all whitespace collapsed.
     */
    public static String collapseWhitespace(String str) {
        StringBuilder result = new StringBuilder();
        char previous = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (isCharAnyOf(c, WHITESPACE_CHARS)) {
                if (previous != ' ') {
                    result.append(' ');
                }
                previous = ' ';
            } else {
                result.append(c);
                previous = c;
            }
        }
        return result.toString();
    }

    /**
     * Returns the first n characters from this string, where n = count. If the string is shorter, the entire string
     * will be returned. If the string is longer, it will be truncated.
     *
     * @param str The string to parse.
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
     * Replaces all occurrences of this originalToken in this string with this replacementToken.
     *
     * @param str The string to process.
     * @param originalToken The token to replace.
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
        return arrayToDelimitedString(",", strings);
    }

    /**
     * Turns this string array in one delimited string.
     *
     * @param delimiter The delimiter to use.
     * @param strings The array to process.
     * @return The new delimited string. An empty string if {@code strings} is empty. {@code null} if strings is {@code null}.
     */
    public static String arrayToDelimitedString(String delimiter, Object[] strings) {
        if (strings == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(strings[i]);
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
     * Splits this string into an array using this delimiter.
     *
     * @param str The string to split.
     * @param delimiter The delimiter to use.
     * @return The resulting array.
     */
    public static String[] tokenizeToStringArray(String str, String delimiter) {
        if (str == null) {
            return null;
        }

        return tokenizeToStringCollection(str, delimiter).toArray(new String[0]);
    }

    /**
     * Splits this string into a collection using this delimiter.
     *
     * @param str The string to split.
     * @param delimiter The delimiter to use.
     * @return The resulting array.
     */
    public static List<String> tokenizeToStringCollection(String str, String delimiter) {
        if (str == null) {
            return null;
        }

        return Arrays.stream(str.split(delimiter)).map(String::trim).collect(Collectors.toList());
    }

    /**
     * Convenience method to return a Collection as a comma-delimited
     * String. e.g. useful for {@code toString()} implementations.
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
     * @param delimiter The delimiter.
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
     * Trim any leading occurrence of this character from the given String.
     *
     * @param str the String to check.
     * @param character The character to trim.
     * @return the trimmed String
     * @see java.lang.Character#isWhitespace
     */
    public static String trimLeadingCharacter(String str, char character) {
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && character == buf.charAt(0)) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

    /**
     * Checks whether this strings both begins with this prefix and ends withs either of these suffixes.
     *
     * @param str The string to check.
     * @param prefix The prefix.
     * @param suffixes The suffixes.
     * @return {@code true} if it does, {@code false} if not.
     */
    public static boolean startsAndEndsWith(String str, String prefix, String... suffixes) {
        if (StringUtils.hasLength(prefix) && !str.startsWith(prefix)) {
            return false;
        }
        for (String suffix : suffixes) {
            if (str.toUpperCase().endsWith(suffix.toUpperCase()) && (str.length() > (prefix + suffix).length())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wrap this string every lineSize characters.
     *
     * @param str The string to wrap.
     * @param lineSize The maximum size of each line.
     * @return The wrapped string.
     */
    public static String wrap(String str, int lineSize) {
        if (str.length() < lineSize) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int oldPos = 0;
        for (int pos = lineSize; pos < str.length(); pos += lineSize) {
            result.append(str, oldPos, pos).append("\n");
            oldPos = pos;
        }
        result.append(str.substring(oldPos));
        return result.toString();
    }

    /**
     * Wrap this string at the word boundary at or below lineSize characters.
     *
     * @param str The string to wrap.
     * @param lineSize The maximum size of each line.
     * @return The word-wrapped string.
     */
    public static String wordWrap(String str, int lineSize) {
        if (str.length() < lineSize) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int oldPos = 0;
        int pos = lineSize;
        while (pos < str.length()) {
            if (Character.isWhitespace(str.charAt(pos))) {
                pos++;
                continue;
            }

            String part = str.substring(oldPos, pos);
            int spacePos = part.lastIndexOf(' ');
            if (spacePos > 0) {
                pos = oldPos + spacePos + 1;
            }

            result.append(str.substring(oldPos, pos).trim()).append("\n");
            oldPos = pos;
            pos += lineSize;
        }
        result.append(str.substring(oldPos));
        return result.toString();
    }

    /**
     * Checks whether this character matches any of these characters.
     *
     * @param c The char to check.
     * @param chars The chars that should match.
     * @return {@code true} if it does, {@code false if not}.
     */
    public static boolean isCharAnyOf(char c, String chars) {
        for (int i = 0; i < chars.length(); i++) {
            if (chars.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    public static Pair<String,String> getFileNameAndExtension(String path) {
        String[] foldersSplit = path.split("[|/\\\\]");
        String fileNameAndExtension = foldersSplit[foldersSplit.length - 1];

        String[] nameExtensionSplit = fileNameAndExtension.split("\\.");
        if (nameExtensionSplit.length < 2) {
            return Pair.of(fileNameAndExtension, "");
        }

        return Pair.of(nameExtensionSplit[nameExtensionSplit.length - 2], nameExtensionSplit[nameExtensionSplit.length - 1]);
    }

    public static Pair<String, String> splitAtFirstSeparator(String input, String separator) {
        int separatorIndex = input.indexOf(separator);
        if (separatorIndex >= 0) {
            return Pair.of(input.substring(0, separatorIndex),
                           input.substring(separatorIndex + separator.length()));
        } else {
            return Pair.of(input, "");
        }
    }

    public static int countOccurrencesOf(String text, char search) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == search) {
                count++;
            }
        }
        return count;
    }

    public static String getDaysString(long days) {
        return days + " day" + pluralizeSuffix(days);
    }

    public static String pluralizeSuffix(long input) {
        return input != 1 ? "s" : "";
    }

    public static String capitalizeFirstLetter(String str) {
        if(!hasText(str)) {
            return "";
        }

        String result = str.substring(0, 1).toUpperCase();
        if (str.length() > 1) {
            result += str.substring(1);
        }
        return result;
    }

    public static String redactedValueStringOfAMap(String value) {
        if (!hasText(value) ||
            !value.startsWith("{") ||
            !value.endsWith("}") ||
            value.length() <= 2) {
            return value;
        }

        value = value.substring(1, value.length() - 1).trim();
        String[] pairs = value.split(",");
        StringBuilder resultBuilder = new StringBuilder();

        for (String s : pairs) {
            String[] pair = s.trim().split("=");

            if(pair.length != 2) {
                continue;
            }

            pair[0] = pair[0].trim();
            pair[1] = pair[1].trim();

            pair[1] = redactValueIfSensitive(pair[0], pair[1]);

            resultBuilder.append(pair[0]).append("=").append(pair[1]).append(",").append(" ");
        }

        String result = resultBuilder.toString().trim();

        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        return "{" + result + "}";
    }

    public static String redactValueIfSensitive(String key, String value) {
        if (!hasText(key) || !hasText(value)) {
            return value;
        }

        if (key.toLowerCase().endsWith("password") || key.toLowerCase().endsWith("token")
            || ConfigUtils.LICENSE_KEY.equalsIgnoreCase(key)) {
            value = "********";
        }

        return value;
    }
}
