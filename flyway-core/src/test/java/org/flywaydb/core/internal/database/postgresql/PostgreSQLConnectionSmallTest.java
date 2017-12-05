/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgreSQLConnectionSmallTest {
    @Test
    public void getFirstSchemaFromSearchPath() {
        assertEquals("ABC", PostgreSQLConnection.getFirstSchemaFromSearchPath("\"ABC\", def"));
    }
}
