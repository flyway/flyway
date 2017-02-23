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

import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for PostgreSQLSqlStatementBuilder.
 */
public class PostgreSQLSqlStatementBuilderSmallTest {
    /**
     * Class under test.
     */
    private PostgreSQLSqlStatementBuilder statementBuilder = new PostgreSQLSqlStatementBuilder();

    @Test
    public void columnColumnText() {
        String sqlScriptSource = "CREATE DOMAIN some_domain AS CHARACTER(3)\n" +
                "CONSTRAINT some_domain_check CHECK (((VALUE) :: TEXT = ANY (ARRAY ['FOO' :: TEXT, 'BAR' :: TEXT])));";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void regclass() {
        String sqlScriptSource = "CREATE TABLE base_table (\n" +
                "base_table_id integer DEFAULT nextval('base_table_seq'::regclass) NOT NULL\n" +
                ");";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void function() {
        String sqlScriptSource = "CREATE FUNCTION add(integer, integer) RETURNS integer\n" +
                "    LANGUAGE sql IMMUTABLE STRICT\n" +
                "    AS $_$select $1 + $2;$_$;\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void ts() {
        String line = "insert into testDate values (TIMESTAMP '2004-10-19 10:23:54')";
        statementBuilder.addLine(line + ";\n");
        assertTrue(statementBuilder.isTerminated());
        assertEquals(line, statementBuilder.getSqlStatement().getSql());
    }

    @Test
    public void eString() {
        String sqlScriptSource = "CREATE TABLE sometable1 (\n" +
                "  id        SERIAL PRIMARY KEY,\n" +
                "  somefield TEXT CHECK (somefield ~ E'.')\n" +
                ");\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void copy() {
        String line = "COPY CSV_FILES FROM '/path/to/filename.csv' DELIMITER ';' CSV HEADER";
        statementBuilder.addLine(line + ";\n");
        assertTrue(statementBuilder.isTerminated());
        assertFalse(statementBuilder.isPgCopyFromStdIn());
        assertEquals(line, statementBuilder.getSqlStatement().getSql());
    }

    @Test
    public void alterType() {
        String line = "ALTER TYPE \"myschema\".\"colors\" ADD ATTRIBUTE f3 int";
        statementBuilder.addLine(line + ";\n");
        assertTrue(statementBuilder.executeInTransaction());
    }

    @Test
    public void alterTypeAddValue() {
        String line = "ALTER TYPE \"myschema\".\"colors\" ADD VALUE 'orange' AFTER 'red'";
        statementBuilder.addLine(line + ";\n");
        assertFalse(statementBuilder.executeInTransaction());
    }

    @Test
    public void multilineStringLiteralWithSemicolons() {
        String sqlScriptSource = "INSERT INTO address VALUES (1, '1. first;\n"
                + "2. second;\n"
                + "3. third;')";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertEquals(sqlScriptSource, statementBuilder.getSqlStatement().getSql());
    }

    @Test
    public void multilineDollar() {
        final String sqlScriptSource =
                "INSERT INTO dollar VALUES($$Hello\n" +
                        "multi-line\n" +
                        "quotes;\n" +
                        "$$)";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertEquals(sqlScriptSource, statementBuilder.getSqlStatement().getSql());
    }

    @Test
    public void multilineDollarNestedQuotes() {
        final String sqlScriptSource =
                "CREATE OR REPLACE FUNCTION upperFunc()\n" +
                        "RETURNS void AS $$\n" +
                        "DECLARE\n" +
                        "var varchar = 'abc';\n" +
                        "BEGIN\n" +
                        "raise info 'upperFunc';\n" +
                        "CREATE OR REPLACE FUNCTION internalFunc()\n" +
                        "RETURNS void AS $BODY$\n" +
                        "DECLARE\n" +
                        "var varchar1 = 'abc';\n" +
                        "BEGIN\n" +
                        "raise info 'internalFunc'\n" +
                        "END;\n" +
                        "$BODY$ LANGUAGE plpgsql;\n" +
                        "END;\n" +
                        "$$ LANGUAGE plpgsql";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertEquals(sqlScriptSource, statementBuilder.getSqlStatement().getSql());
    }

    @Test
    public void dollarQuoteRegex() {
        assertFalse("abc".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertFalse("abc$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertFalse("$abc".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$abc$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$ABC$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$aBcDeF$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$aBc_DeF$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$abcDEF123$".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$abcDEF123$xxx".matches(PostgreSQLSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
    }
}