/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for StandardSqlScript.
 */
public class StandardSqlScriptTest {
    @Test
    public void linesToStatements() {
        List<String> lines = new ArrayList<String>();
        lines.add("select col1, col2");
        lines.add("from mytable");
        lines.add("where col1 > 10;");

        List<SqlStatement> sqlStatements = new StandardSqlScript().linesToStatements(lines);
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
        assertEquals("select col1, col2 from mytable where col1 > 10", sqlStatement.getSql());
    }
}
