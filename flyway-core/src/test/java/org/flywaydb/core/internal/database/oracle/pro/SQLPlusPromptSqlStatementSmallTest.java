package org.flywaydb.core.internal.database.oracle.pro;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLPlusPromptSqlStatementSmallTest {
    @Test
    public void transformSql() {
        assertEquals("abc", new SQLPlusPromptSqlStatement(1,"PRO abc").transformSql());
        assertEquals("abc", new SQLPlusPromptSqlStatement(1,"PROMPT abc").transformSql());
    }

    @Test
    public void transformSqlMultiline() {
        assertEquals("abc\ndef", new SQLPlusPromptSqlStatement(1,"PROMPT abc-\ndef").transformSql());
        assertEquals("abc\ndef-", new SQLPlusPromptSqlStatement(1,"PROMPT abc-\ndef-").transformSql());
    }
}
