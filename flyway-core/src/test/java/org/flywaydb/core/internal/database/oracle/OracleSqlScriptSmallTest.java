/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for OracleSqlScript.
 */
public class OracleSqlScriptSmallTest {
    private SqlScript createSqlScript(String source) {
        return new SqlScript(source, null) {
            @Override
            protected SqlStatementBuilder createSqlStatementBuilder() {
                return new OracleSqlStatementBuilder(Delimiter.SEMICOLON);
            }
        };
    }

    @Test
    public void parseSqlStatements() throws Exception {
        String source = new ClassPathResource("migration/database/oracle/sql/placeholders/V1__Placeholders.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(8, sqlStatements.get(0).getLineNumber());
        assertEquals(17, sqlStatements.get(1).getLineNumber());
        assertEquals(22, sqlStatements.get(2).getLineNumber());
        assertEquals("COMMIT", sqlStatements.get(2).getSql());
    }

    @Test
    public void parseSqlStatementsWithInlineCommentsInsidePlSqlBlocks() throws Exception {
        String source = new ClassPathResource("migration/database/oracle/sql/function/V2__FunctionWithConditionals.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
        assertEquals(8, sqlStatements.get(0).getLineNumber());
        assertTrue(sqlStatements.get(0).getSql().contains("/* for the rich */"));
    }

    @Test
    public void parseFunctionsAndProcedures() throws Exception {
        String source = new ClassPathResource("migration/database/oracle/sql/function/V1__Function.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(6, sqlStatements.size());
        assertEquals(7, sqlStatements.get(0).getLineNumber());
        assertEquals(18, sqlStatements.get(1).getLineNumber());
        assertEquals(26, sqlStatements.get(2).getLineNumber());
        assertEquals(30, sqlStatements.get(3).getLineNumber());
        assertEquals(38, sqlStatements.get(4).getLineNumber());
        assertEquals("COMMIT", sqlStatements.get(4).getSql());
        assertEquals(40, sqlStatements.get(5).getLineNumber());
    }

    @Test
    public void parsePackages() throws Exception {
        String source = new ClassPathResource("migration/database/oracle/sql/package/V1__Package.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(4, sqlStatements.size());
        assertEquals(7, sqlStatements.get(0).getLineNumber());
        assertEquals(23, sqlStatements.get(1).getLineNumber());
        assertEquals(30, sqlStatements.get(2).getLineNumber());
        assertEquals(41, sqlStatements.get(3).getLineNumber());
    }

    @Test
    public void parseQQuotes() throws Exception {
        String source = new ClassPathResource("migration/database/oracle/sql/qquote/V1__Q_Quote.sql",
                Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = createSqlScript(source);
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

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
    }

    @Test
    public void parseMergeInsert() throws Exception {
        String source = "INSERT INTO ss.CODETBL(FIELDNAME,FIELDVALUE,IDS)\n" +
                "SELECT FIELDNAME,FIELDVALUE,IDS FROM\n" +
                "(\n" +
                "SELECT 'ACCT_TYPE_CD' FIELDNAME, '$' FIELDVALUE,'SAMP' IDS FROM DUAL UNION ALL\n" +
                "SELECT 'ACCT_TYPE_CD', 'L','SAMP' FROM DUAL UNION ALL\n" +
                "SELECT 'ACCT_TYPE_CD', 'C','SAMP' FROM DUAL \n" +
                ")\n" +
                "D\n" +
                "WHERE NOT EXISTS\n" +
                "(\n" +
                "SELECT 1 FROM SS.CODETBL \n" +
                "WHERE D.FIELDNAME = FIELDNAME \n" +
                "AND D.FIELDVALUE = FIELDVALUE\n" +
                "AND D.IDS = IDS\n" +
                ");";

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
    }

    @Test
    public void parseProcedure() throws Exception {
        String source = "CREATE OR REPLACE PROCEDURE set_right_value_for_sequence(seq_name in VARCHAR2, table_name in VARCHAR2, column_id in VARCHAR2)\n" +
                "IS\n" +
                "    seq_val NUMBER(6);\n" +
                "    row_count NUMBER(6);\n" +
                "BEGIN\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'select ' || seq_name || '.nextval from dual' INTO seq_val;\n" +
                "\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'alter sequence  ' || seq_name || ' increment by -' || seq_val || ' minvalue 0';\n" +
                "\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'select ' || seq_name || '.nextval from dual' INTO seq_val;\n" +
                "\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'select case when max(' || column_id || ') is null then 1 else max(' || column_id || ') end from ' || table_name INTO row_count;\n" +
                "\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'alter sequence ' || seq_name || ' increment by ' || row_count || ' minvalue 0';\n" +
                "\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'select ' || seq_name || '.nextval from dual' INTO seq_val;\n" +
                "\n" +
                "    EXECUTE IMMEDIATE\n" +
                "    'alter sequence ' || seq_name || ' increment by 1 minvalue 1';\n" +
                "END;\n" +
                "/\n" +
                "\n" +
                "EXECUTE set_right_value_for_sequence('SEQ_ATR', 'TOTCATTRIB', 'ATTRIB_ID');";

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(2, sqlStatements.size());
    }
}
