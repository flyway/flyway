/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.dbsupport.oracle;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for OracleSqlStatementBuilder.
 */
public class OracleSqlStatementBuilderSmallTest {
    private OracleSqlStatementBuilder builder = new OracleSqlStatementBuilder();

    @Test
    public void changeDelimiterRegEx() {
        assertNull(builder.changeDelimiterIfNecessary("BEGIN_DATE", null));
        assertEquals("/", builder.changeDelimiterIfNecessary("BEGIN DATE", null).getDelimiter());
        assertEquals("/", builder.changeDelimiterIfNecessary("BEGIN", null).getDelimiter());
    }

    @Test
    public void nvarchar() {
        builder.addLine("INSERT INTO nvarchar2_test VALUES ( N'qwerty' );");
        assertTrue(builder.isTerminated());
    }
}
