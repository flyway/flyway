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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test for SQLServerSqlStatementBuilder.
 */
public class SQLServerSqlStatementBuilderSmallTest {
    /**
     * Class under test.
     */
    private SQLServerSqlStatementBuilder statementBuilder = new SQLServerSqlStatementBuilder(new Delimiter("GO", true));

    @Test
    public void go() {
        String sqlScriptSource = "DROP VIEW dbo.TESTVIEW\n" +
                "GO\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void likeNoSpace() {
        String sqlScriptSource = "ALTER TRIGGER CUSTOMER_INSERT ON CUSTOMER AFTER\n" +
                "INSERT AS\n" +
                "BEGIN\n" +
                "    SELECT TOP 1 1 FROM CUSTOMER WHERE [name] LIKE'%test';\n" +
                "END\n" +
                "GO";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }
}