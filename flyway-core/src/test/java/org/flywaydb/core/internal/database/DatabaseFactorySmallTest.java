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
package org.flywaydb.core.internal.database;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseFactorySmallTest {
    @Test
    public void filterUrl() {
        assertEquals("jdbc:postgresql://host/db", DatabaseFactory.filterUrl("jdbc:postgresql://host/db"));
        assertEquals("jdbc:postgresql://host/db", DatabaseFactory.filterUrl("jdbc:postgresql://admin:<password>@host/db"));
        assertEquals("jdbc:postgresql://host/db", DatabaseFactory.filterUrl("jdbc:postgresql://host/db?user=admin&password=<password>"));
        assertEquals("jdbc:sqlserver:////host:1234;databaseName=db", DatabaseFactory.filterUrl("jdbc:sqlserver:////host:1234;databaseName=db"));
        assertEquals("jdbc:sap://localhost:62060/?databaseName=HXE", DatabaseFactory.filterUrl("jdbc:sap://localhost:62060/?databaseName=HXE"));
    }
}
