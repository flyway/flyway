/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.redshift;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RedshiftConnectionSmallTest {
    @Test
    public void getFirstSchemaFromSearchPath() {
        assertEquals("ABC", RedshiftConnection.getFirstSchemaFromSearchPath("\"ABC\", def"));
    }

    @Test
    public void getFirstSchemaFromSearchPathDollarUser() {
        assertEquals("public", RedshiftConnection.getFirstSchemaFromSearchPath("$user,public"));
        assertEquals("public", RedshiftConnection.getFirstSchemaFromSearchPath("\"$user\",public"));
        assertEquals("public", RedshiftConnection.getFirstSchemaFromSearchPath("\"$user\",\"public\""));
    }
}
