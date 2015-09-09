/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.SqlStatement;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for OracleSqlScript.
 */
public class OracleSqlScriptSmallTest {
    @Test
    public void parseSqlStatements() throws Exception {
        String source = new ClassPathResource("migration/dbsupport/oracle/sql/placeholders/V1__Placeholders.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(18, sqlStatements.get(0).getLineNumber());
        assertEquals(27, sqlStatements.get(1).getLineNumber());
        assertEquals(32, sqlStatements.get(2).getLineNumber());
        assertEquals("COMMIT", sqlStatements.get(2).getSql());
    }

    @Test
    public void parseSqlStatementsWithInlineCommentsInsidePlSqlBlocks() throws Exception {
        String source = new ClassPathResource("migration/dbsupport/oracle/sql/function/V2__FunctionWithConditionals.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
        assertEquals(18, sqlStatements.get(0).getLineNumber());
        assertTrue(sqlStatements.get(0).getSql().contains("/* for the rich */"));
    }

    @Test
    public void parseFunctionsAndProcedures() throws Exception {
        String source = new ClassPathResource("migration/dbsupport/oracle/sql/function/V1__Function.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(5, sqlStatements.size());
        assertEquals(17, sqlStatements.get(0).getLineNumber());
        assertEquals(26, sqlStatements.get(1).getLineNumber());
        assertEquals(34, sqlStatements.get(2).getLineNumber());
        assertEquals(36, sqlStatements.get(3).getLineNumber());
        assertEquals(44, sqlStatements.get(4).getLineNumber());
        assertEquals("COMMIT", sqlStatements.get(4).getSql());
    }

    @Test
    public void parsePackages() throws Exception {
        String source = new ClassPathResource("migration/dbsupport/oracle/sql/package/V1__Package.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(2, sqlStatements.size());
        assertEquals(17, sqlStatements.get(0).getLineNumber());
        assertEquals(33, sqlStatements.get(1).getLineNumber());
    }

    @Test
    public void parseQQuotes() throws Exception {
        String source = new ClassPathResource("migration/dbsupport/oracle/sql/qquote/V1__Q_Quote.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(12, sqlStatements.size());
    }

    @Test
    public void parseCompoundTrigger() throws Exception {
        String source = "CREATE OR REPLACE TRIGGER triggername\n" +
                "  FOR insert ON tablename\n" +
                "    COMPOUND TRIGGER\n" +
                "\n" +
                "  -- Global declaration.\n" +
                "  g_global_variable VARCHAR2(10);\n" +
                "\n" +
                "  BEFORE STATEMENT IS\n" +
                "  BEGIN\n" +
                "    NULL; -- Do something here.\n" +
                "  END BEFORE STATEMENT;\n" +
                "\n" +
                "  BEFORE EACH ROW IS\n" +
                "  BEGIN\n" +
                "    NULL; -- Do something here.\n" +
                "  END BEFORE EACH ROW;\n" +
                "\n" +
                "  AFTER EACH ROW IS\n" +
                "  BEGIN\n" +
                "    NULL; -- Do something here.\n" +
                "  END AFTER EACH ROW;\n" +
                "\n" +
                "  AFTER STATEMENT IS\n" +
                "  BEGIN\n" +
                "    NULL; -- Do something here.\n" +
                "  END AFTER STATEMENT;\n" +
                "\n" +
                "END <trigger-name>;\n" +
                "/";

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
    }
}
