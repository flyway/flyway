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
