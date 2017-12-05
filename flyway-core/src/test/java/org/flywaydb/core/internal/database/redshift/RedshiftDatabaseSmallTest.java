/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.redshift;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RedshiftDatabaseSmallTest {
    @Test
    public void doQuote() {
        assertEquals("\"abc\"", RedshiftDatabase.redshiftQuote("abc"));
        assertEquals("\"a\"\"b\"\"c\"", RedshiftDatabase.redshiftQuote("a\"b\"c"));
    }
}
