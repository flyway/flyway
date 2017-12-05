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
package org.flywaydb.core.internal.database.oracle.pro;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLPlusSetSqlStatementSmallTest {
    @Test
    public void nullText() {
        assertEquals("", SQLPlusSetSqlStatement.getNullText("NULL \"\""));
        assertEquals("abc", SQLPlusSetSqlStatement.getNullText("NULL abc"));
        assertEquals("abc", SQLPlusSetSqlStatement.getNullText("NULL \"abc\""));
    }
}
