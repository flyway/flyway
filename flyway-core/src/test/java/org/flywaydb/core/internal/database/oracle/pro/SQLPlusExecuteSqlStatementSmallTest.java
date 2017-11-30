/*
 * Copyright 2010-2017 Boxfuse GmbH
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
