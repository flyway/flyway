/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
