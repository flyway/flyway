/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.mysql.MySQLDbSupport;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for SqlScript.
 */
public class SqlScriptSmallTest {

    /**
     * Class under test.
     */
    private SqlScript sqlScript = new SqlScript(new MySQLDbSupport(null));

    /**
     * Input lines.
     */
    private List<String> lines = new ArrayList<String>();

    @Test
    public void stripSqlCommentsNoComment() {
        lines.add("select * from table;");
        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertEquals("select * from table", sqlStatements.get(0).getSql());
    }

    @Test
    public void stripSqlCommentsSingleLineComment() {
        lines.add("--select * from table;");
        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertEquals(0, sqlStatements.size());
    }

    @Test
    public void stripSqlCommentsMultiLineCommentSingleLine() {
        lines.add("/*comment line*/");
        lines.add("select * from table;");
        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertEquals("select * from table", sqlStatements.get(0).getSql());
    }

    @Test
    public void stripSqlCommentsMultiLineCommentMultipleLines() {
        lines.add("/*comment line");
        lines.add("more comment text*/");
        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertEquals(0, sqlStatements.size());
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

    @Test
    public void linesToStatementsMySQLCommentDirectives() {
        lines.add("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
        lines.add("DROP TABLE IF EXISTS account;");
        lines.add("/*!40101 SET character_set_client = utf8 */;");

        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertNotNull(sqlStatements);
        assertEquals(3, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
        assertEquals("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */", sqlStatement.getSql());
    }

    @Test(timeout = 1000)
    public void linesToStatementsSuperLongStatement() {
        lines.add("INSERT INTO T1 (A, B, C, D) VALUES");
        for (int i = 0; i < 10000; i++) {
            lines.add("(1, '2', '3', '4'),");
        }
        lines.add("(1, '2', '3', '4');");

        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
    }

    @Test
    public void linesToStatementsPreserveEmptyLinesInsideStatement() {
        lines.add("update emailtemplate set body = 'Hi $order.billingContactDisplayName,");
        lines.add("");
        lines.add("Thanks for your interest in our products!");
        lines.add("");
        lines.add("Please find your quote attached in PDF format.'");
        lines.add("where templatename = 'quote_template';");

        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
        assertEquals("update emailtemplate set body = 'Hi $order.billingContactDisplayName,\n" +
                "\n" +
                "Thanks for your interest in our products!\n" +
                "\n" +
                "Please find your quote attached in PDF format.'\n" +
                "where templatename = 'quote_template'", sqlStatement.getSql());
    }

    @Test
    public void linesToStatementsSkipEmptyLinesBetweenStatements() {
        lines.add("update emailtemplate set body = 'Hi';");
        lines.add("");
        lines.add("update emailtemplate set body = 'Hello';");
        lines.add("");
        lines.add("");
        lines.add("update emailtemplate set body = 'Howdy';");

        List<SqlStatement> sqlStatements = sqlScript.linesToStatements(lines);
        assertNotNull(sqlStatements);
        assertEquals(3, sqlStatements.size());

        assertEquals(1, sqlStatements.get(0).getLineNumber());
        assertEquals("update emailtemplate set body = 'Hi'", sqlStatements.get(0).getSql());

        assertEquals(3, sqlStatements.get(1).getLineNumber());
        assertEquals("update emailtemplate set body = 'Hello'", sqlStatements.get(1).getSql());

        assertEquals(6, sqlStatements.get(2).getLineNumber());
        assertEquals("update emailtemplate set body = 'Howdy'", sqlStatements.get(2).getSql());
    }

    @Test
    public void parsePlaceholderComments() {
        String source = "${drop_view} \"SOME_VIEW\" IF EXISTS;\n" + "CREATE ${or_replace} VIEW \"SOME_VIEW\";\n";

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("drop_view", "--");
        placeholders.put("or_replace", "OR REPLACE");

        List<SqlStatement> sqlStatements = sqlScript.parse(source, new PlaceholderReplacer(placeholders, "${", "}"));
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(2, sqlStatement.getLineNumber());
        assertEquals("CREATE OR REPLACE VIEW \"SOME_VIEW\"", sqlStatement.getSql());
    }

    @Test
    public void parseNoTrim() {
        String source = "update emailtemplate set body = 'Hi $order.billingContactDisplayName,\n" +
                "\n" +
                "    Thanks for your interest in our products!\n" +
                "\n" +
                "    Please find your quote attached in PDF format.'\n" +
                "where templatename = 'quote_template'";

        List<SqlStatement> sqlStatements = sqlScript.parse(source, PlaceholderReplacer.NO_PLACEHOLDERS);
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
        assertEquals(source, sqlStatement.getSql());
    }

    @Test
    public void parsePreserveTrailingCommentsInsideStatement() {
        String source = "update emailtemplate /* yes, it's true */\n" +
                "    set   body='Thanks !' /* my pleasure */\n" +
                "  and  subject = 'To our favorite customer!'";

        List<SqlStatement> sqlStatements = sqlScript.parse(source, PlaceholderReplacer.NO_PLACEHOLDERS);
        assertNotNull(sqlStatements);
        assertEquals(1, sqlStatements.size());

        SqlStatement sqlStatement = sqlStatements.get(0);
        assertEquals(1, sqlStatement.getLineNumber());
        assertEquals(source, sqlStatement.getSql());
    }
}
