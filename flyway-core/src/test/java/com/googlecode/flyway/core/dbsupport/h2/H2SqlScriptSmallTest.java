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
        H2SqlScript script = new H2SqlScript();
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t;"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='xyz';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a= 'xyz';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='xyz'"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='$$';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='xy''z';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='xyz' and b like 'abc%';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a=' xyz ';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a=';';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='$$''$$';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='$$' || '$$';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("select * from t where a='$$'||'$$';"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. T');"));
        assertFalse(script.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon;');"));
        assertTrue(script.endsWithOpenMultilineStringLiteral("select * from t where a='$$||''$$;"));
        assertTrue(script.endsWithOpenMultilineStringLiteral("select * from t where a='"));
        assertTrue(script.endsWithOpenMultilineStringLiteral("select * from t where a='abc"));
        assertTrue(script.endsWithOpenMultilineStringLiteral("select * from t where a='abc''"));
        assertTrue(script.endsWithOpenMultilineStringLiteral("select * from t where a='abc'''||'"));
        assertTrue(script.endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon+Linebreak;"));
    }
}
