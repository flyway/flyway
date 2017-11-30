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
