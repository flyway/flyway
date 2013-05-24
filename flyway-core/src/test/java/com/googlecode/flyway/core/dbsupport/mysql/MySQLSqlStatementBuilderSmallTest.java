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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.dbsupport.oracle.OracleSqlStatementBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for MySQLSqlStatementBuilder.
 */
public class MySQLSqlStatementBuilderSmallTest {
    @Test
    public void isCommentDirective() {
        final MySQLSqlStatementBuilder statementBuilder = new MySQLSqlStatementBuilder();
        assertFalse(statementBuilder.isCommentDirective("SELECT * FROM TABLE;"));
        assertFalse(statementBuilder.isCommentDirective("/*SELECT * FROM TABLE*/;"));
        assertTrue(statementBuilder.isCommentDirective("/*!12345 SELECT * FROM TABLE*/"));
        assertTrue(statementBuilder.isCommentDirective("/*!12345 SELECT * FROM TABLE*/;"));
    }
}
