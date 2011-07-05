/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.util;

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
            result += " ";
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
        if (str == null) {
            return false;
        }
        return str.matches("\\d*");
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
}
