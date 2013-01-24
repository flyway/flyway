/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.ingres;

import com.googlecode.flyway.core.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for IngresSqlStatementBuilder.
 */
public class IngresSqlStatementBuilderSmallTest {
    @Test
    public void endsWithOpenMultilineStringLiteral() {
        assertTrue(new IngresSqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO address VALUES (1, '1. first"));
        assertFalse(new IngresSqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO address VALUES (1, '1. first\n" +
                "2. second');"));
    }

    @Test
    public void multilineStringLiteralWithSemicolons() {
        String sqlScriptSource = "INSERT INTO address VALUES (1, '1. first;\n"
                + "2. second;\n"
                + "3. third;')";

        IngresSqlStatementBuilder statementBuilder = new IngresSqlStatementBuilder();

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

        IngresSqlStatementBuilder statementBuilder = new IngresSqlStatementBuilder();

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

        IngresSqlStatementBuilder statementBuilder = new IngresSqlStatementBuilder();

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertEquals(sqlScriptSource, statementBuilder.getSqlStatement().getSql());
    }

    @Test
    public void dollarQuoteRegex() {
        assertFalse("abc".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertFalse("abc$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertFalse("$abc".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$abc$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$ABC$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$aBcDeF$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$aBc_DeF$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$abcDEF123$".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
        assertTrue("$abcDEF123$xxx".matches(IngresSqlStatementBuilder.DOLLAR_QUOTE_REGEX));
    }
}