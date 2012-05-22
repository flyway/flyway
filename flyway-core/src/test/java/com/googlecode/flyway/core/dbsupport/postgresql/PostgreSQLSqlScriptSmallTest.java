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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for PostgreSQLSqlScript.
 */
public class PostgreSQLSqlScriptSmallTest {
    @Test
    public void endsWithOpenMultilineStringLiteral() {
        final PostgreSQLSqlScript script = new PostgreSQLSqlScript("", PlaceholderReplacer.NO_PLACEHOLDERS);
        assertTrue(script.endsWithOpenMultilineStringLiteral("INSERT INTO address VALUES (1, '1. first"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("INSERT INTO address VALUES (1, '1. first\n" +
                "2. second');"));
    }

    @Test
    public void multilineStringLiteralWithSemicolons() {
        String sqlScriptSource = "INSERT INTO address VALUES (1, '1. first;\n"
                + "2. second;\n"
                + "3. third;')";
        final PostgreSQLSqlScript script = new PostgreSQLSqlScript(sqlScriptSource, PlaceholderReplacer.NO_PLACEHOLDERS);
        assertEquals(1, script.getSqlStatements().size());
        assertEquals(sqlScriptSource, script.getSqlStatements().get(0).getSql());
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
                "RETURNS void AS $body$\n" +
                "DECLARE\n" +
                "var varchar1 = 'abc';\n" +
                "BEGIN\n" +
                "raise info 'internalFunc'\n" +
                "END;\n" +
                "$body$ LANGUAGE plpgsql;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql";
        final PostgreSQLSqlScript script = new PostgreSQLSqlScript(sqlScriptSource, PlaceholderReplacer.NO_PLACEHOLDERS);
        assertEquals(1, script.getSqlStatements().size());
        assertEquals(sqlScriptSource, script.getSqlStatements().get(0).getSql());

    }
}
