/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgreSQLDatabaseSmallTest {
    @Test
    public void doQuote() {
        assertEquals("\"abc\"", PostgreSQLDatabase.pgQuote("abc"));
        assertEquals("\"a\"\"b\"\"c\"", PostgreSQLDatabase.pgQuote("a\"b\"c"));
    }
}
