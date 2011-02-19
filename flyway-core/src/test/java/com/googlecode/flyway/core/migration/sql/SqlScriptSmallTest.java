/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.migration.sql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for SqlScript.
 */
public class SqlScriptSmallTest {

    /**
     * Class under test.
     */
    private SqlScript sqlScript = new SqlScript();

    /**
     * Input lines.
     */
    private List<String> lines = new ArrayList<String>();

    @Test
    public void stripSqlCommentsNoComment() {
        lines.add("select * from table;");
        List<String> result = sqlScript.stripSqlComments(lines);
        assertEquals("select * from table;", result.get(0));
    }

    @Test
    public void stripSqlCommentsSingleLineComment() {
        lines.add("--select * from table;");
        List<String> result = sqlScript.stripSqlComments(lines);
        assertEquals("", result.get(0));
    }

    @Test
    public void stripSqlCommentsMultiLineCommentSingleLine() {
        lines.add("/*comment line*/");
        lines.add("select * from table;");
        List<String> result = sqlScript.stripSqlComments(lines);
        assertEquals("", result.get(0));
        assertEquals("select * from table;", result.get(1));
    }

    @Test
    public void stripSqlCommentsMultiLineCommentMultipleLines() {
        lines.add("/*comment line");
        lines.add("more comment text*/");
        List<String> result = sqlScript.stripSqlComments(lines);
        assertEquals("", result.get(0));
        assertEquals("", result.get(1));
    }

    @Test
    public void linesToStatements() {
        lines.add("select col1, col2");
        lines.add("from mytable");
        lines.add("where col1 > 10;");

        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
        assertEquals("select col1, col2\nfrom mytable\nwhere col1 > 10", sqlStatement.getSql());
    }
}
