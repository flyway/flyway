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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testcase for StringUtils.
 */
public class StringUtilsSmallTest {
    @Test
    public void trimOrPad() {
        assertEquals("Hello World    ", StringUtils.trimOrPad("Hello World", 15));
        assertEquals("Hello Worl", StringUtils.trimOrPad("Hello World", 10));
        assertEquals("          ", StringUtils.trimOrPad(null, 10));
    }

    @Test
    public void isNumeric() {
        assertFalse(StringUtils.isNumeric(null));
        assertTrue(StringUtils.isNumeric(""));
        assertFalse(StringUtils.isNumeric("  "));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12 3"));
        assertFalse(StringUtils.isNumeric("ab2c"));
        assertFalse(StringUtils.isNumeric("12-3"));
        assertFalse(StringUtils.isNumeric("12.3"));
    }

    @Test
    public void collapseWhitespace() {
        assertEquals("", StringUtils.collapseWhitespace(""));
        assertEquals("abc", StringUtils.collapseWhitespace("abc"));
        assertEquals("a b", StringUtils.collapseWhitespace("a b"));
        assertEquals(" a ", StringUtils.collapseWhitespace(" a "));
        assertEquals(" a ", StringUtils.collapseWhitespace("  a  "));
        assertEquals("a b", StringUtils.collapseWhitespace("a          b"));
        assertEquals("a b c", StringUtils.collapseWhitespace("a  b   c"));
        assertEquals(" a b c ", StringUtils.collapseWhitespace("   a b   c  "));
    }

    @Test
    public void tokenizeToStringArray() {
        assertArrayEquals(new String[]{"abc"}, StringUtils.tokenizeToStringArray("abc", ","));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.tokenizeToStringArray("abc,def", ","));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.tokenizeToStringArray(" abc ,def ", ","));
        assertArrayEquals(new String[]{"", "abc"}, StringUtils.tokenizeToStringArray(",abc", ","));
        assertArrayEquals(new String[]{"", "abc"}, StringUtils.tokenizeToStringArray(" , abc", ","));
    }
}
