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
package com.googlecode.flyway.core.dbsupport.h2;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for H2SqlScript
 */
public class H2SqlScriptSmallTest {
    @Test
    public void endsWithOpenMultilineStringLiteral() {
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t;"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='xyz';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a= 'xyz';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='xyz'"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='xy''z';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='xyz' and b like 'abc%';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a=' xyz ';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a=';';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='$$''$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='$$' || '$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral("select * from t where a='$$'||'$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. T');"));
        assertFalse(endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon;');"));
        assertFalse(endsWithOpenMultilineStringLiteral("INSERT INTO test_user (id, name) VALUES (1, 'Mr. Semicolon;');"));
        assertFalse(endsWithOpenMultilineStringLiteral("insert into TAB1 (GUID, UID, VAL) values (1, '0100', 100);"));
        assertTrue(endsWithOpenMultilineStringLiteral("select * from t where a='$$||''$$;"));
        assertTrue(endsWithOpenMultilineStringLiteral("select * from t where a='"));
        assertTrue(endsWithOpenMultilineStringLiteral("select * from t where a='abc"));
        assertTrue(endsWithOpenMultilineStringLiteral("select * from t where a='abc''"));
        assertTrue(endsWithOpenMultilineStringLiteral("select * from t where a='abc'''||'"));
        assertTrue(endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon+Linebreak;"));
    }

    private boolean endsWithOpenMultilineStringLiteral(String sql) {
        H2SqlStatementBuilder statementBuilder = new H2SqlStatementBuilder();
        return statementBuilder.endsWithOpenMultilineStringLiteral(sql);
    }
}
