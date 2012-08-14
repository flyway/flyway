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
        H2SqlStatementBuilder statementBuilder = new H2SqlStatementBuilder();
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t;"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='xyz';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a= 'xyz';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='xyz'"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='$$';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='xy''z';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='xyz' and b like 'abc%';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a=' xyz ';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a=';';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='$$''$$';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='$$' || '$$';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='$$'||'$$';"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. T');"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon;');"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (id, name) VALUES (1, 'Mr. Semicolon;');"));
        assertFalse(statementBuilder.endsWithOpenMultilineStringLiteral("insert into TAB1 (GUID, UID, VAL) values (1, '0100', 100);"));
        assertTrue(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='$$||''$$;"));
        assertTrue(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='"));
        assertTrue(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='abc"));
        assertTrue(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='abc''"));
        assertTrue(statementBuilder.endsWithOpenMultilineStringLiteral("select * from t where a='abc'''||'"));
        assertTrue(statementBuilder.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon+Linebreak;"));
    }
}
