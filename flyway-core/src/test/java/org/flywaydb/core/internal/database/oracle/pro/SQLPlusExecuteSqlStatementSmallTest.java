/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle.pro;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLPlusExecuteSqlStatementSmallTest {
    @Test
    public void transformSql() {
        assertEquals(
                "BEGIN\n" +
                        "USERNAME.PA_SECURITY.SP_TBL_PLC_ADD('V_NAME', USERNAME.PA_SECURITY.POLICY_NAME);\n" +
                        "END;",
                new SQLPlusExecuteSqlStatement(1,
                        "EXEC USERNAME.PA_SECURITY.SP_TBL_PLC_ADD('V_NAME', USERNAME.PA_SECURITY.POLICY_NAME)"
                ).transformSql());
    }

    @Test
    public void transformSqlMultiline() {
        assertEquals(
                "BEGIN\n" +
                        "USERNAME.PA_SECURITY.SP_TBL_PLC_ADD('V_NAME', \n" +
                        "USERNAME.PA_SECURITY.POLICY_NAME);\n" +
                        "END;",
                new SQLPlusExecuteSqlStatement(1,
                        "EXEC USERNAME.PA_SECURITY.SP_TBL_PLC_ADD('V_NAME', -\n" +
                                "USERNAME.PA_SECURITY.POLICY_NAME)"
                ).transformSql());
    }
}
