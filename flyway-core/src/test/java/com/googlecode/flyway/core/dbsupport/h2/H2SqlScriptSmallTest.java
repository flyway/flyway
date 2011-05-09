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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import com.googlecode.flyway.core.util.ResourceUtils;

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

	@Test
	public void correctStatementSplitting()
	{
		Resource sql = new ClassPathResource("com/googlecode/flyway/core/dbsupport/h2/insert-test.sql");
		String sqlAsString = ResourceUtils.loadResourceAsString(sql, "ISO-8859-1");
		SqlScript script = new H2SqlScript(sqlAsString, new PlaceholderReplacer(new HashMap<String, String>(), "${",
				"}"));
		List<SqlStatement> statements = script.getSqlStatements();
		Assert.assertEquals(3, statements.size());
	}
}
