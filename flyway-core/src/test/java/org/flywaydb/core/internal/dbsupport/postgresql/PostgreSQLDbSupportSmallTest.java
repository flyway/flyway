/**
 * Copyright 2010-2016 Boxfuse GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
    @Test
    public void doQuote() {
        PostgreSQLDbSupport dbSupport = new PostgreSQLDbSupport(null);
        assertEquals("\"abc\"", dbSupport.doQuote("abc"));
        assertEquals("\"a\"\"b\"\"c\"", dbSupport.doQuote("a\"b\"c"));
    }

    @Test
    public void undoQuote() {
        PostgreSQLDbSupport dbSupport = new PostgreSQLDbSupport(null);
        assertEquals("abc", dbSupport.unQuote("\"abc\""));
        assertEquals("abc", dbSupport.unQuote("abc\""));
        assertEquals("abc", dbSupport.unQuote("\"abc"));
        assertEquals("a\"b\"c", dbSupport.unQuote("a\"b\"c"));
        assertEquals("a\"b\"c", dbSupport.unQuote("\"a\"b\"c"));
    }
}
