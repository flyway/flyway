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
package org.flywaydb.core.internal.dbsupport.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgreSQLDbSupportSmallTest {
    private final PostgreSQLDbSupport dbSupport = new PostgreSQLDbSupport(null);

    @Test
    public void doQuote() {
        assertEquals("\"abc\"", dbSupport.doQuote("abc"));
        assertEquals("\"a\"\"b\"\"c\"", dbSupport.doQuote("a\"b\"c"));
    }

    @Test
    public void getFirstSchemaFromSearchPath() {
        assertEquals("ABC", dbSupport.getFirstSchemaFromSearchPath("\"ABC\", def"));
    }
}
