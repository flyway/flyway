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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AsciiTableSmallTest {
    @Test
    public void render() {
        List<String> columns = Arrays.asList("A", "Xyz", "xxxxx");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("a", "a", "a"),
                Arrays.asList("a12345", "a", "a1234"),
                Arrays.asList("a", "a", "a"),
                Arrays.asList("a", null, "a")
        );
        assertEquals(
                "+--------+-----+-------+\n" +
                        "| A      | Xyz | xxxxx |\n" +
                        "+--------+-----+-------+\n" +
                        "| a      | a   | a     |\n" +
                        "| a12345 | a   | a1234 |\n" +
                        "| a      | a   | a     |\n" +
                        "| a      | *** | a     |\n" +
                        "+--------+-----+-------+\n",
                new AsciiTable(columns, rows, "***", "Super empty!").render());
    }

    @Test
    public void renderEmpty() {
        List<String> columns = Arrays.asList("A", "Xyz", "xxxxx");
        List<List<String>> rows = Collections.emptyList();
        assertEquals(
                "+---+-----+-------+\n" +
                        "| A | Xyz | xxxxx |\n" +
                        "+---+-----+-------+\n" +
                        "| Super empty!    |\n" +
                        "+---+-----+-------+\n",
                new AsciiTable(columns, rows, "", "Super empty!").render());
    }
}
