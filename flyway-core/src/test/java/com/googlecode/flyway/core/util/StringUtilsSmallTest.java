package com.googlecode.flyway.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for StringUtils.
 */
public class StringUtilsSmallTest {
    @Test
    public void trimOrPad() {
        assertEquals("Hello World    ", StringUtils.trimOrPad("Hello World", 15));
        assertEquals("Hello Worl", StringUtils.trimOrPad("Hello World", 10));
    }
}
