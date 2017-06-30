package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class DB2SqlStatementBuilderSmallTest {
    @Test
    public void isBegin() throws Exception {
        assertTrue(DB2SqlStatementBuilder.isBegin("BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL:BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL: BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL :BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL : BEGIN"));
    }

    @Test
    public void isTerminated() {
        DB2SqlStatementBuilder builder = new DB2SqlStatementBuilder();
        String sqlScriptSource = "CREATE OR REPLACE PROCEDURE IP_ROLLUP(\n" +
                "  IN iODLID INTEGER,\n" +
                "  IN iNDLID INTEGER,\n" +
                "  IN iOID INTEGER,\n" +
                "  IN iTYPE VARCHAR(5),\n" +
                "  IN iACTION VARCHAR(6)\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC SP_IP_ROLLUP\n" +
                "MAIN : BEGIN\n" +
                "\n" +
                "\n" +
                "END\n" +
                "@";

        builder.setDelimiter(new Delimiter("@", false));
        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }
}