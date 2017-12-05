/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import static org.junit.Assert.assertEquals;

/**
 * Small test for TimeFormat
 */
public class TimeFormatSmallTest {
    @Test
    public void format() {
        assertEquals("00:00.001s", TimeFormat.format(1));
        assertEquals("00:00.012s", TimeFormat.format(12));
        assertEquals("00:00.123s", TimeFormat.format(123));
        assertEquals("00:01.234s", TimeFormat.format(1234));
        assertEquals("00:12.345s", TimeFormat.format(12345));
        assertEquals("01:23.456s", TimeFormat.format(60000 + 23456));
        assertEquals("12:34.567s", TimeFormat.format((60000 * 12) + 34567));
        assertEquals("123:45.678s", TimeFormat.format((60000 * 123) + 45678));
    }
}
