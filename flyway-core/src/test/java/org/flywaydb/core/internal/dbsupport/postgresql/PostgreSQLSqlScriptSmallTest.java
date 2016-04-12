/**
 * Copyright 2010-2016 Boxfuse GmbH
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

import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.SqlStatement;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for SqlScript for PostgreSQL.
 */
public class PostgreSQLSqlScriptSmallTest {
    @Test
    public void parseSqlStatementsDo() throws Exception {
        String source = new ClassPathResource(
                "migration/dbsupport/postgresql/sql/dollar/V2__Even_more_dollars.sql", Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = new SqlScript(source, new PostgreSQLDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(17, sqlStatements.get(0).getLineNumber());
        assertEquals(23, sqlStatements.get(1).getLineNumber());
        assertEquals(28, sqlStatements.get(2).getLineNumber());
    }
}
